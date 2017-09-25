/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.sos.rest.events.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.springframework.cloud.client.hypermedia.RemoteResource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestOperations;

/**
 * @author Oliver Gierke
 */
@Slf4j
@RequiredArgsConstructor
public class ConfigurableEventClient implements EventClient {

	private final IntegrationOperations operations;
	private final RestOperations client;
	private final Map<ParameterizedTypeReference<?>, RemoteResource> resources = new HashMap<>();

	public void add(ParameterizedTypeReference<?> type, RemoteResource resource) {
		this.resources.put(type, resource);
	}

	/*
	 * (non-Javadoc)
	 * @see example.sos.rest.events.client.EventClient#doWithIntegration(org.springframework.core.ParameterizedTypeReference, java.util.function.BiConsumer)
	 */
	@Override
	public <T> void doWithIntegration(ParameterizedTypeReference<T> type, BiConsumer<T, IntegrationOperations> callback) {

		RemoteResource resource = resources.get(type);

		Optional.ofNullable(resource) //
				.map(it -> it.getLink()) //
				.ifPresent(it -> {

					Integration integration = operations.findUniqueIntegration();

					Map<String, Object> parameters = new HashMap<>();
					parameters.put("type", "productAdded");

					integration.getLastUpdate()
							.ifPresent(lastUpdate -> parameters.put("since", lastUpdate.format(DateTimeFormatter.ISO_DATE_TIME)));

					URI uri = URI.create(it.expand(parameters).getHref());

					log.info("Requesting new events from {}…", uri);

					RequestEntity<Void> request = RequestEntity.get(uri) //
							.accept(MediaTypes.HAL_JSON) //
							.build();

					callback.accept(client.exchange(request, type).getBody(), operations);
				});
	}
}
