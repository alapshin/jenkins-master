package main.groovy.com.alapshin.jenkins.init
/*
   Copyright 2015-2017 Sam Gleske 
   https://github.com/samrocketman/jenkins-bootstrap-jervis

   Changes to load config from YAML file and other improvements by Andrei Lapshin

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

import org.yaml.snakeyaml.Yaml

import java.util.logging.Logger
import jenkins.model.*
import jenkins.plugins.git.GitSCMSource
import jenkins.plugins.git.traits.BranchDiscoveryTrait
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever

/*
   Configure pipeline shared libraries in the global Jenkins configuration.
   This will safely compare configured libraries and only overwrite the global
   shared library config if changes have been made.
 */

logger = Logger.getLogger("")
config = new File(Jenkins.instance.getRootDir(), "config.yml").withInputStream {
    return new Yaml().load(it)
}

if (!config.libraries) {
    logger.info('Nothing changed. No global libraries settings are found.')
    return
}


List<LibraryConfiguration> libraries = []
config.libraries.each { config ->
    def scm = new GitSCMSource(config.retriever.scm.git.remote).with {
        traits = [new BranchDiscoveryTrait()]
        credentialsId = config.retriever.scm.git.credentialsId
        return it
    }
    def retriever = new SCMSourceRetriever(scm)
    libraries << new LibraryConfiguration(config.name, retriever).with {
        implicit = config.get('implicit', false)
        defaultVersion = config['defaultVersion']
        includeInChangesets = config.get('includeInChangesets', false)
        allowVersionOverride = config.get('allowVersionOverride', true)
        return it
    }
}

GlobalLibraries pluginSettings = GlobalLibraries.get()

if (libraries && !isLibrariesEqual(pluginSettings.libraries, libraries)) {
    pluginSettings.libraries = libraries
    pluginSettings.save()
    logger.info('Configured Pipeline Global Shared Libraries:\n    ' 
        + pluginSettings.libraries.collect { it.name }.join('\n    '))
} else {
    logger.info('Nothing changed. Pipeline Global Shared Libraries already configured.')
}

/**
  Function to compare if the two global shared libraries are equal.
 */
private static boolean isLibrariesEqual(List<LibraryConfiguration> lib1, List<LibraryConfiguration> lib2) {
    // Compare returns true or false
    lib1.size() == lib2.size() &&
    (
        [lib1, lib2].transpose().every { l1, l2 ->
            def s1 = l1.retriever.scm
            def s2 = l2.retriever.scm

            l1.retriever.class == l2.retriever.class &&
            l1.name == l2.name &&
            l1.defaultVersion == l2.defaultVersion &&
            l1.implicit == l2.implicit &&
            l1.allowVersionOverride == l2.allowVersionOverride &&
            l1.includeInChangesets == l2.includeInChangesets &&
            s1.remote == s2.remote &&
            s1.credentialsId == s2.credentialsId &&
            s1.traits.size() == s2.traits.size() &&
            (
                [s1.traits, s2.traits].transpose().every { t1, t2 ->
                    t1.class == t2.class
                }
            )
        }
    )
}

