/*
   Copyright 2015-2017 Sam Gleske
   https://github.com/samrocketman/jenkins-bootstrap-jervis

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

package com.alapshin.jenkins.init

import org.yaml.snakeyaml.Yaml

import java.util.logging.Logger
import jenkins.model.*
import org.jenkinsci.plugins.github.GitHubPlugin
import org.jenkinsci.plugins.github.config.GitHubPluginConfig
import org.jenkinsci.plugins.github.config.GitHubServerConfig
import org.jenkinsci.plugins.github.config.HookSecretConfig

/*
   Configure GitHub plugin for GitHub servers
 */
logger = Logger.getLogger("")

config = new File(Jenkins.instance.getRootDir(), "config.yml").withInputStream {
    return new Yaml().load(it)
}

if (!config.github) {
    logger.info('Nothing changed. GitHub plugin settings not found found.')
    return
}

List<GitHubServerConfig> configs = 
    config.github['servers'].collect { server ->
        return new GitHubServerConfig(server['credentialsId']).with {
            name = server['name']
            apiUrl = server.get('apiUrl', 'https://api.github.com')
            manageHooks = server.get('manageHooks', false)
            clientCacheSize = 20

            return it
        }
    }

def pluginSettings = GitHubPlugin.configuration()
def incomingSettings = new GitHubPluginConfig(configs).with {
    if (config.github['hookUrl']) {
        hookUrl = new URL(config.github['hookUrl'])
            overrideHookUrl = true
    }
    hookSecretConfig = new HookSecretConfig(config.github['hookSharedSecretId'])
    return it
}

def overrideHookEqual = isOverrideHookEqual(pluginSettings, incomingSettings)

def serverConfigsEqual = isServerConfigsEqual(pluginSettings.configs, 
    incomingSettings.configs)
def pluginSettingsEqual = isPluginSettingsEqual(pluginSettings, incomingSettings) 

def hookSecretConfigEqual = pluginSettings.hookSecretConfig.credentialsId ==
    incomingSettings.hookSecretConfig.credentialsId

if (pluginSettingsEqual && serverConfigsEqual) {
    logger.info('Nothing changed. GitHub plugin already configured.')
} else {
    // Update hook shared secret if it is changed
    if (pluginSettings.hookSecretConfig && !hookSecretConfigEqual) {
        pluginSettings.hookSecretConfig = incomingSettings.hookSecretConfig
    }
    if (!overrideHookEqual) {
        if (pluginSettings.isOverrideHookURL() && !incommingSettings.isOverrideHookURL()) {
            pluginSettings.hookUrl = null
        } else if (pluginSettings.hookUrl != incomingSettings.hookUrl) {
            pluginSettings.hookUrl = incomingSettings.hookUrl
        }
    }
    if (!serverConfigsEqual) {
        pluginSettings.configs = configs
    }
    pluginSettings.save()
    logger.info('Configured GitHub plugin.')
}

private boolean isServerConfigsEqual(List<GitHubServerConfig> s1, List<GitHubServerConfig> s2) {
    return s1.size() == s2.size() && 
        [s1, s2].transpose().every { c1, c2 ->
            c1.name == c2.name && 
            c1.apiUrl == c2.apiUrl &&
            c1.manageHooks == c2.manageHooks &&
            c1.credentialsId == c2.credentialsId &&
            c1.clientCacheSize == c2.clientCacheSize
        }
}

private boolean isOverrideHookEqual(GitHubPluginConfig settings, GitHubPluginConfig config) {
    (
        (
            config.isOverrideHookURL() &&
            settings.isOverrideHookURL() &&
            settings.hookUrl == config.hookUrl
        ) ||
        (
            !settings.isOverrideHookURL() && !config.isOverrideHookURL()
        )
    )
}

private boolean isPluginSettingsEqual(GitHubPluginConfig settings, GitHubPluginConfig config) {
    settings.hookSecretConfig &&
        settings.hookSecretConfig.credentialsId == config.hookSecretConfig.credentialsId &&
            isOverrideHookEqual(settings, config)
}

