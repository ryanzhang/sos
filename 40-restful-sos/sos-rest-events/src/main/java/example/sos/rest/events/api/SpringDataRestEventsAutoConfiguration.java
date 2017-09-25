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
package example.sos.rest.events.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ResourceProcessor;

/**
 * @author Oliver Gierke
 */
@Configuration
class SpringDataRestEventsAutoConfiguration {

	@Bean
	@ConditionalOnClass(ResourceProcessor.class)
	EventResourceProcessor eventResourceProcessor(PersistentEntities entities, EntityLinks entityLinks) {
		return new EventResourceProcessor(entities, entityLinks);
	}

	@Bean
	@ConditionalOnClass(RepositoryLinksResource.class)
	EventsResourceProcessor eventsResourceProcessor() {
		return new EventsResourceProcessor();
	}
}
