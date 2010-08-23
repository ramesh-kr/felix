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

package org.apache.felix.sigil.common.config;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface IRepositoryConfig
{
    static final String REPOSITORY_PROVIDER = "provider";
    static final String WILD_CARD = "*";
    
    /**
     * Return the ordered list of repositories to search
     * for dependencies.
     * 
     * @return
     */
    List<String> getRepositoryPath();

    /**
     * get properties with which to instantiate repositories.
     * The key REPOSITORY_PROVIDER will be set to the fully qualified class name of the IRepositoryProvider.
     * @return
     */
    Map<String, Properties> getRepositoryConfig();
}
