/**
 * Copyright 2013 Automatak, LLC
 *
 * Licensed to Automatak, LLC under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Automatak, LLC licenses this file to you
 * under the GNU Affero General Public License Version 3.0 (the "License");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/agpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.automatak.dnp3.tools.testset;

import com.automatak.dnp3.tools.pluginapi.MasterPluginFactory;
import com.automatak.dnp3.tools.pluginapi.OutstationPluginFactory;

import java.util.LinkedList;
import java.util.List;

public class PluginConfiguration {

    private final List<OutstationPluginFactory> outstations;
    private final List<MasterPluginFactory> masters;

    public PluginConfiguration()
    {
        outstations = new LinkedList<OutstationPluginFactory>();
        masters = new LinkedList<MasterPluginFactory>();
    }

    public PluginConfiguration(List<OutstationPluginFactory> outstations, List<MasterPluginFactory> masters) {
        this.outstations = outstations;
        this.masters = masters;
    }

    public List<OutstationPluginFactory> getOutstations() {
        return outstations;
    }

    public List<MasterPluginFactory> getMasters() {
        return masters;
    }
}