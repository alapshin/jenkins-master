import io.jenkins.plugins.artifact_manager_jclouds.s3.S3BlobStoreConfig
import jenkins.model.ArtifactManagerConfiguration
import io.jenkins.plugins.artifact_manager_jclouds.s3.S3BlobStore
import io.jenkins.plugins.artifact_manager_jclouds.JCloudsArtifactManagerFactory

def factories = ArtifactManagerConfiguration.get().artifactManagerFactories
if (factories.isEmpty() && Boolean.getBoolean("s3-artifact-manager.enabled")) {
    factories.add(new JCloudsArtifactManagerFactory(new S3BlobStore()))
}
