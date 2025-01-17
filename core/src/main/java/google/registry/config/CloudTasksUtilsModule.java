// Copyright 2021 The Nomulus Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package google.registry.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.cloud.tasks.v2.CloudTasksSettings;
import dagger.Module;
import dagger.Provides;
import google.registry.batch.CloudTasksUtils;
import google.registry.batch.CloudTasksUtils.GcpCloudTasksClient;
import google.registry.batch.CloudTasksUtils.SerializableCloudTasksClient;
import google.registry.config.CredentialModule.DefaultCredential;
import google.registry.config.RegistryConfig.Config;
import google.registry.util.GoogleCredentialsBundle;
import java.io.IOException;
import java.io.Serializable;
import java.util.function.Supplier;

/**
 * A {@link Module} that provides {@link CloudTasksUtils}.
 *
 * <p>The class itself cannot be annotated as {@code Inject} because its requested dependencies use
 * the {@link Config} qualifier which is not available in the {@code util} package.
 */
@Module
public abstract class CloudTasksUtilsModule {

  // Provides a supplier instead of using a Dagger @Provider because the latter is not serializable.
  @Provides
  public static Supplier<CloudTasksClient> provideCloudTasksClientSupplier(
      @DefaultCredential GoogleCredentialsBundle credentials) {
    return (Supplier<CloudTasksClient> & Serializable)
        () -> {
          CloudTasksClient client;
          try {
            client =
                CloudTasksClient.create(
                    CloudTasksSettings.newBuilder()
                        .setCredentialsProvider(
                            FixedCredentialsProvider.create(credentials.getGoogleCredentials()))
                        .build());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          return client;
        };
  }

  @Provides
  public static SerializableCloudTasksClient provideSerializableCloudTasksClient(
      final Supplier<CloudTasksClient> clientSupplier) {
    return new GcpCloudTasksClient(clientSupplier);
  }
}
