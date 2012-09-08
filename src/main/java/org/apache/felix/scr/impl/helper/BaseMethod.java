/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.scr.impl.helper;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;


/**
 * Component method to be invoked on service (un)binding.
 */
abstract class BaseMethod
{

    // class references to simplify parameter checking
    protected static final Class COMPONENT_CONTEXT_CLASS = ComponentContext.class;
    protected static final Class BUNDLE_CONTEXT_CLASS = BundleContext.class;
    protected static final Class SERVICE_REFERENCE_CLASS = ServiceReference.class;
    protected static final Class MAP_CLASS = Map.class;
    protected static final Class INTEGER_CLASS = Integer.class;

    private final SimpleLogger m_logger;
    private final boolean isDS11;
    private final boolean isDS12Felix;

    private final String m_methodName;
    private final Class m_componentClass;

    private volatile Method m_method;

    private final boolean m_methodRequired;

    private volatile State m_state;

    protected BaseMethod( final SimpleLogger logger, final String methodName,
            final Class componentClass, final boolean ds11, final boolean ds12Felix )
    {
        this( logger, methodName, methodName != null, componentClass, ds11, ds12Felix );
    }


    protected BaseMethod( final SimpleLogger logger, final String methodName,
            final boolean methodRequired, final Class componentClass, final boolean ds11, final boolean ds12Felix )
    {
        m_logger = logger;
        m_methodName = methodName;
        m_methodRequired = methodRequired;
        m_componentClass = componentClass;
        isDS11 = ds11;
        isDS12Felix = ds12Felix;
        if ( m_methodName == null )
        {
            m_state = NotApplicable.INSTANCE;
        }
        else
        {
            m_state = NotResolved.INSTANCE;
        }
    }


    protected final SimpleLogger getLogger()
    {
        return m_logger;
    }


    protected final boolean isDS11()
    {
        return isDS11;
    }


    protected final boolean isDS12Felix()
    {
        return isDS12Felix;
    }


    protected final String getMethodName()
    {
        return m_methodName;
    }

    protected final Method getMethod()
    {
        return m_method;
    }

    protected final Class getComponentClass()
    {
        return m_componentClass;
    }


    synchronized void setMethod( Method method )
    {
        this.m_method = method;

        if ( method != null )
        {
            m_state = Resolved.INSTANCE;
            getLogger().log( LogService.LOG_DEBUG, "Found {0} method: {1}", new Object[]
                { getMethodNamePrefix(), method }, null );
        }
        else if ( m_methodRequired )
        {
            m_state = NotFound.INSTANCE;
            getLogger().log(LogService.LOG_ERROR, "{0} method [{1}] not found; Component will fail",
                new Object[]
                    { getMethodNamePrefix(), getMethodName() }, null);
        }
        else
        {
            // optional method not found, log as DEBUG and ignore
            getLogger().log( LogService.LOG_DEBUG, "{0} method [{1}] not found, ignoring", new Object[]
                { getMethodNamePrefix(), getMethodName() }, null );
            m_state = NotApplicable.INSTANCE;
        }
    }


    State getState()
    {
        return m_state;
    }


    /**
     * Finds the method named in the {@link #m_methodName} field in the given
     * <code>targetClass</code>. If the target class has no acceptable method
     * the class hierarchy is traversed until a method is found or the root
     * of the class hierarchy is reached without finding a method.
     *
     * @return The requested method or <code>null</code> if no acceptable method
     *      can be found in the target class or any super class.
     * @throws InvocationTargetException If an unexpected Throwable is caught
     *      trying to find the requested method.
     */
    private Method findMethod() throws InvocationTargetException
    {
        boolean acceptPrivate = isDS11();
        boolean acceptPackage = isDS11();

        final Class targetClass = getComponentClass();
        final ClassLoader targetClasslLoader = targetClass.getClassLoader();
        final String targetPackage = getPackageName( targetClass );

        for ( Class theClass = targetClass; theClass != null; )
        {

            if ( getLogger().isLogEnabled( LogService.LOG_DEBUG ) )
            {
                getLogger().log( LogService.LOG_DEBUG,
                    "Locating method " + getMethodName() + " in class " + theClass.getName(), null );
            }

            try
            {
                Method method = doFindMethod( theClass, acceptPrivate, acceptPackage );
                if ( method != null )
                {
                    return method;
                }
            }
            catch ( SuitableMethodNotAccessibleException ex )
            {
                // log and return null
                getLogger().log( LogService.LOG_ERROR,
                    "findMethod: Suitable but non-accessible method {0} found in class {1}, subclass of {2}", new Object[]
                        { getMethodName(), theClass.getName(), targetClass.getName() }, null );
                break;
            }

            // if we get here, we have no method, so check the super class
            theClass = theClass.getSuperclass();
            if ( theClass == null )
            {
                break;
            }

            // super class method check ignores private methods and accepts
            // package methods only if in the same package and package
            // methods are (still) allowed
            acceptPackage &= targetClasslLoader == theClass.getClassLoader()
                && targetPackage.equals( getPackageName( theClass ) );

            // private methods will not be accepted any more in super classes
            acceptPrivate = false;
        }

        // nothing found after all these years ...
        return null;
    }


    protected abstract Method doFindMethod( final Class targetClass, final boolean acceptPrivate,
        final boolean acceptPackage ) throws SuitableMethodNotAccessibleException, InvocationTargetException;


    private MethodResult invokeMethod( final Object componentInstance, final Object rawParameter )
        throws InvocationTargetException
    {
        try
        {
            if ( componentInstance != null )
            {
                final Object[] params = getParameters(m_method, rawParameter);
                Object result = m_method.invoke(componentInstance, params);
                return new MethodResult((m_method.getReturnType() != Void.TYPE), (Map) result);
            }
            else
            {
                getLogger().log( LogService.LOG_WARNING, "Method {0} cannot be called on null object",
                    new Object[]
                        { getMethodName() }, null );
            }
        }
        catch ( IllegalStateException ise )
        {
            getLogger().log( LogService.LOG_DEBUG, ise.getMessage(), null );
            return null;
        }
        catch ( IllegalAccessException ex )
        {
            // 112.3.1 If the method is not is not declared protected or
            // public, SCR must log an error message with the log service,
            // if present, and ignore the method
            getLogger().log( LogService.LOG_DEBUG, "Method {0} cannot be called", new Object[]
                { getMethodName() }, ex );
        }
        catch ( InvocationTargetException ex )
        {
            throw ex;
        }
        catch ( Throwable t )
        {
            throw new InvocationTargetException( t );
        }

        // assume success (also if the mehotd is not available or accessible)
        return MethodResult.VOID; // TODO: or null ??
    }

    protected boolean returnValue()
    {
        // allow returning Map if declared as DS 1.2-Felix or newer
        return isDS12Felix();
    }

    /**
     * Returns the parameter array created from the <code>rawParameter</code>
     * using the actual parameter type list of the <code>method</code>.
     * @param method
     * @param rawParameter
     * @return
     * @throws IllegalStateException If the required parameters cannot be
     *      extracted from the <code>rawParameter</code>
     */
    protected abstract Object[] getParameters( Method method, Object rawParameter );


    protected String getMethodNamePrefix()
    {
        return "";
    }


    //---------- Helpers

    /**
     * Finds the named public or protected method in the given class or any
     * super class. If such a method is found, its accessibility is enfored by
     * calling the <code>Method.setAccessible</code> method if required and
     * the method is returned. Enforcing accessibility is required to support
     * invocation of protected methods.
     *
     * @param clazz The <code>Class</code> which provides the method.
     * @param name The name of the method.
     * @param parameterTypes The parameters to the method. Passing
     *      <code>null</code> is equivalent to using an empty array.
     *
     * @return The named method with enforced accessibility or <code>null</code>
     *      if no such method exists in the class.
     *
     * @throws SuitableMethodNotAccessibleException If method with the given
     *      name taking the parameters is found in the class but the method
     *      is not accessible.
     * @throws InvocationTargetException If an unexpected Throwable is caught
     *      trying to access the desired method.
     */
    public /* static */ Method getMethod( Class clazz, String name, Class[] parameterTypes, boolean acceptPrivate,
        boolean acceptPackage ) throws SuitableMethodNotAccessibleException,
        InvocationTargetException
    {
        try
        {
            // find the declared method in this class
            Method method = clazz.getDeclaredMethod( name, parameterTypes );

            // accept public and protected methods only and ensure accessibility
            if ( accept( method, acceptPrivate, acceptPackage, returnValue() ) )
            {
                return method;
            }

            // the method would fit the requirements but is not acceptable
            throw new SuitableMethodNotAccessibleException();
        }
        catch ( NoSuchMethodException nsme )
        {
            // thrown if no method is declared with the given name and
            // parameters
            if ( getLogger().isLogEnabled( LogService.LOG_DEBUG ) )
            {
                String argList = ( parameterTypes != null ) ? Arrays.asList( parameterTypes ).toString() : "";
                getLogger().log( LogService.LOG_DEBUG, "Declared Method {0}.{1}({2}) not found", new Object[]
                    { clazz.getName(), name, argList }, null );
            }
        }
        catch ( NoClassDefFoundError cdfe )
        {
            // may be thrown if a method would be found but the signature
            // contains throws declaration for an exception which cannot
            // be loaded
            if ( getLogger().isLogEnabled( LogService.LOG_WARNING ) )
            {
                StringBuffer buf = new StringBuffer();
                buf.append( "Failure loooking up method " ).append( name ).append( '(' );
                for ( int i = 0; parameterTypes != null && i < parameterTypes.length; i++ )
                {
                    buf.append( parameterTypes[i].getName() );
                    if ( i > 0 )
                    {
                        buf.append( ", " );
                    }
                }
                buf.append( ") in class class " ).append( clazz.getName() ).append( ". Assuming no such method." );
                getLogger().log( LogService.LOG_WARNING, buf.toString(), cdfe );
            }
        }
        catch ( SuitableMethodNotAccessibleException e)
        {
            throw e;
        }
        catch ( Throwable throwable )
        {
            // unexpected problem accessing the method, don't let everything
            // blow up in this situation, just throw a declared exception
            throw new InvocationTargetException( throwable, "Unexpected problem trying to get method " + name );
        }

        // caught and ignored exception, assume no method and continue search
        return null;
    }


    /**
     * Returns <code>true</code> if the method is acceptable to be returned from the
     * {@link #getMethod(Class, String, Class[], boolean, boolean)} and also
     * makes the method accessible.
     * <p>
     * This method returns <code>true</code> iff:
     * <ul>
     * <li>The method has <code>void</code> return type</li>
     * <li>Is not static</li>
     * <li>Is public or protected</li>
     * <li>Is private and <code>acceptPrivate</code> is <code>true</code></li>
     * <li>Is package private and <code>acceptPackage</code> is <code>true</code></li>
     * </ul>
     * <p>
     * This method is package private for unit testing purposes. It is not
     * meant to be called from client code.
     *
     *
     * @param method The method to check
     * @param acceptPrivate Whether a private method is acceptable
     * @param acceptPackage Whether a package private method is acceptable
     * @param allowReturnValue whether the method can return a value (to update service registration properties)
     * @return whether the method is acceptable
     */
    static boolean accept( Method method, boolean acceptPrivate, boolean acceptPackage, boolean allowReturnValue )
    {
        if (!(Void.TYPE == method.getReturnType() || (MAP_CLASS == method.getReturnType() && allowReturnValue)))
        {
            return false;
        }

        // check modifiers now
        int mod = method.getModifiers();

        // no static method
        if ( Modifier.isStatic( mod ) )
        {
            return false;
        }

        // accept public and protected methods
        if ( Modifier.isPublic( mod ) || Modifier.isProtected( mod ) )
        {
            method.setAccessible( true );
            return true;
        }

        // accept private if accepted
        if ( Modifier.isPrivate( mod ) )
        {
            if ( acceptPrivate )
            {
                method.setAccessible( acceptPrivate );
                return true;
            }

            return false;
        }

        // accept default (package)
        if ( acceptPackage )
        {
            method.setAccessible( true );
            return true;
        }

        // else don't accept
        return false;
    }


    /**
     * Returns the name of the package to which the class belongs or an
     * empty string if the class is in the default package.
     */
    public static String getPackageName( Class clazz )
    {
        String name = clazz.getName();
        int dot = name.lastIndexOf( '.' );
        return ( dot > 0 ) ? name.substring( 0, dot ) : "";
    }


    //---------- State management  ------------------------------------

    /**
     * Calls the declared method on the given component with the provided
     * method call arguments.
     *
     *
     *
     * @param componentInstance The component instance on which to call the
     *      method
     * @param rawParameter The parameter container providing the actual
     *      parameters to provide to the called method
     * @param methodCallFailureResult The result to return from this method if
     *      calling the method resulted in an exception.
     *
     * @return <code>true</code> if the method was called successfully or the
     *      method was not found and was not required. <code>false</code> if
     *      the method was not found but required.
     *      <code>methodCallFailureResult</code> is returned if the method was
     *      found and called, but the method threw an exception.
     */
    public MethodResult invoke( final Object componentInstance, final Object rawParameter,
            final MethodResult methodCallFailureResult )
    {
        try
        {
            return m_state.invoke( this, componentInstance, rawParameter );
        }
        catch ( InvocationTargetException ite )
        {
            getLogger().log( LogService.LOG_ERROR, "The {0} method has thrown an exception", new Object[]
                { getMethodName() }, ite.getCause() );
        }

        return methodCallFailureResult;
    }


    public boolean methodExists()
    {
        return m_state.methodExists( this );
    }

    private static interface State
    {

        MethodResult invoke( final BaseMethod baseMethod, final Object componentInstance, final Object rawParameter )
            throws InvocationTargetException;


        boolean methodExists( final BaseMethod baseMethod );
    }

    private static class NotApplicable implements State
    {

        private static final State INSTANCE = new NotApplicable();


        public MethodResult invoke( final BaseMethod baseMethod, final Object componentInstance, final Object rawParameter )
        {
            return MethodResult.VOID;
        }


        public boolean methodExists( final BaseMethod baseMethod )
        {
            return true;
        }
    }

    private static class NotResolved implements State
    {
        private static final State INSTANCE = new NotResolved();


        private void resolve( final BaseMethod baseMethod )
        {
            baseMethod.getLogger().log( LogService.LOG_DEBUG, "getting {0}: {1}", new Object[]
                    {baseMethod.getMethodNamePrefix(), baseMethod.getMethodName()}, null );

            // resolve the method
            Method method;
            try
            {
                method = baseMethod.findMethod();
            }
            catch ( InvocationTargetException ex )
            {
                method = null;
                baseMethod.getLogger().log( LogService.LOG_WARNING, "{0} cannot be found", new Object[]
                        {baseMethod.getMethodName()}, ex.getTargetException() );
            }

            baseMethod.setMethod( method );
        }


        public MethodResult invoke( final BaseMethod baseMethod, final Object componentInstance, final Object rawParameter )
            throws InvocationTargetException
        {
            resolve( baseMethod );
            return baseMethod.getState().invoke( baseMethod, componentInstance, rawParameter );
        }


        public boolean methodExists( final BaseMethod baseMethod )
        {
            resolve( baseMethod );
            return baseMethod.getState().methodExists( baseMethod );
        }
    }

    private static class NotFound implements State
    {
        private static final State INSTANCE = new NotFound();


        public MethodResult invoke( final BaseMethod baseMethod, final Object componentInstance, final Object rawParameter )
        {
            // 112.3.1 If the method is not found , SCR must log an error
            // message with the log service, if present, and ignore the
            // method
            baseMethod.getLogger().log( LogService.LOG_ERROR, "{0} method [{1}] not found", new Object[]
                { baseMethod.getMethodNamePrefix(), baseMethod.getMethodName() }, null );
            return null;
        }


        public boolean methodExists( final BaseMethod baseMethod )
        {
            return false;
        }
    }

    private static class Resolved implements State
    {
        private static final State INSTANCE = new Resolved();


        public MethodResult invoke( final BaseMethod baseMethod, final Object componentInstance, final Object rawParameter )
            throws InvocationTargetException
        {
            baseMethod.getLogger().log( LogService.LOG_DEBUG, "invoking {0}: {1}", new Object[]
                { baseMethod.getMethodNamePrefix(), baseMethod.getMethodName() }, null );
            return baseMethod.invokeMethod( componentInstance, rawParameter );
        }


        public boolean methodExists( final BaseMethod baseMethod )
        {
            return true;
        }
    }
}
