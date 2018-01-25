import jenkins.model.*
import org.yaml.snakeyaml.Yaml

new File(Jenkins.instance.getRootDir(), "config.yml").withInputStream { 
        return new Yaml().load(it)
}
