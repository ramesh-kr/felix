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
package org.apache.felix.scrplugin.om;

import java.util.List;

import org.apache.felix.scrplugin.tags.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;

/**
 * <code>Reference.java</code>...
 *
 */
public class Reference extends AbstractObject {

    protected String name;
    protected String interfacename;
    protected String target;
    protected String cardinality;
    protected String policy;
    protected String bind;
    protected String unbind;

    /** @since 1.0.9 */
    protected String strategy;

    /** Is this reference already checked? */
    protected boolean checked = false;

    /** The class description containing this reference. */
    protected final JavaClassDescription javaClassDescription;

    /**
     * Default constructor.
     */
    public Reference() {
        this(null, null);
    }

    /**
     * Constructor from java source.
     */
    public Reference(JavaTag t, JavaClassDescription desc) {
        super(t);
        this.javaClassDescription = desc;
        // set default values
        this.setBind("bind");
        this.setUnbind("unbind");
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInterfacename() {
        return this.interfacename;
    }

    public void setInterfacename(String interfacename) {
        this.interfacename = interfacename;
    }

    public String getTarget() {
        return this.target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getCardinality() {
        return this.cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
    }

    public String getPolicy() {
        return this.policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getBind() {
        return this.bind;
    }

    public void setBind(String bind) {
        this.bind = bind;
    }

    public String getUnbind() {
        return this.unbind;
    }

    public void setUnbind(String unbind) {
        this.unbind = unbind;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    /** @since 1.0.9 */
    public String getStrategy() {
        return strategy;
    }

    /** @since 1.0.9 */
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    /** @since 1.0.9 */
    public boolean isLookupStrategy() {
        return "lookup".equals(getStrategy());
    }

    /**
     * Validate the property.
     * If errors occur a message is added to the issues list,
     * warnings can be added to the warnings list.
     */
    public void validate(List<String> issues, List<String> warnings, boolean componentIsAbstract)
    throws MojoExecutionException {
        // if this reference is already checked, return immediately
        if ( this.checked ) {
            return;
        }
        final int currentIssueCount = issues.size();

        // validate name
        if (StringUtils.isEmpty(this.name)) {
            issues.add(this.getMessage("Reference has no name"));
        }

        // validate interface
        if (StringUtils.isEmpty(this.interfacename)) {
            issues.add(this.getMessage("Missing interface name"));
        }

        // validate cardinality
        if (this.cardinality == null) {
            this.cardinality = "1..1";
        } else if (!"0..1".equals(this.cardinality) && !"1..1".equals(this.cardinality)
            && !"0..n".equals(this.cardinality) && !"1..n".equals(this.cardinality)) {
            issues.add(this.getMessage("Invalid Cardinality specification " + this.cardinality));
        }

        // validate policy
        if (this.policy == null) {
            this.policy = "static";
        } else if (!"static".equals(this.policy) && !"dynamic".equals(this.policy)) {
            issues.add(this.getMessage("Invalid Policy specification " + this.policy));
        }

        // validate strategy
        if (this.strategy == null) {
            this.strategy = "event";
        } else if (!"event".equals(this.strategy) && !"lookup".equals(this.strategy)) {
            issues.add(this.getMessage("Invalid startegy type " + this.strategy));
        }

        // validate bind and unbind methods
        if (!isLookupStrategy()) {
            final String oldBind = this.bind;
            final String oldUnbind = this.unbind;
            this.bind = this.validateMethod(this.bind, issues, warnings, componentIsAbstract);
            this.unbind = this.validateMethod(this.unbind, issues, warnings, componentIsAbstract);
            if ( issues.size() == currentIssueCount ) {
                if ( this.bind != null && this.unbind != null ) {
                    // no errors, so we're checked
                    this.checked = true;
                } else {
                    if ( this.bind == null ) {
                        this.bind = oldBind;
                    }
                    if ( this.unbind == null ) {
                        this.unbind = oldUnbind;
                    }
                }
            }
        } else {
            this.bind = null;
            this.unbind = null;
        }
    }

    protected String validateMethod(String       methodName,
                                    List<String> issues,
                                    List<String> warnings,
                                    boolean componentIsAbstract)
    throws MojoExecutionException {
        final JavaMethod method = this.findMethod(methodName);
        if (method == null) {
            if ( !componentIsAbstract ) {
                issues.add(this.getMessage("Missing method " + methodName + " for reference " + this.getName()));
            }
            return null;
        }

        if (method.isPublic()) {
            warnings.add(this.getMessage("Method " + method.getName() + " should be declared protected"));
        } else if (!method.isProtected()) {
            issues.add(this.getMessage("Method " + method.getName() + " has wrong qualifier, public or protected required"));
            return null;
        }

        return method.getName();
    }

    public JavaMethod findMethod(String methodName)
    throws MojoExecutionException {
        String[] sig = new String[]{ this.getInterfacename() };
        String[] sig2 = new String[]{ "org.osgi.framework.ServiceReference" };

        // service interface or ServiceReference first
        String realMethodName = methodName;
        JavaMethod method = this.javaClassDescription.getMethodBySignature(realMethodName, sig);
        if (method == null) {
            method = this.javaClassDescription.getMethodBySignature(realMethodName, sig2);
        }

        // append reference name with service interface and ServiceReference
        if (method == null) {
            realMethodName = methodName + Character.toUpperCase(this.name.charAt(0))
            + this.name.substring(1);

            method = this.javaClassDescription.getMethodBySignature(realMethodName, sig);
        }
        if (method == null) {
            method = this.javaClassDescription.getMethodBySignature(realMethodName, sig2);
        }

        // append type name with service interface and ServiceReference
        if (method == null) {
            int lastDot = this.getInterfacename().lastIndexOf('.');
            realMethodName = methodName
                + this.getInterfacename().substring(lastDot + 1);
            method = this.javaClassDescription.getMethodBySignature(realMethodName, sig);
        }
        if (method == null) {
            method = this.javaClassDescription.getMethodBySignature(realMethodName, sig2);
        }

        return method;
    }

}