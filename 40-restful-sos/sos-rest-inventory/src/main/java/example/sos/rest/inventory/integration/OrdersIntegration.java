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
package example.sos.rest.inventory.integration;

import example.sos.rest.events.client.EventClient;
import example.sos.rest.events.client.Integration;
import example.sos.rest.inventory.Inventory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.TypeReferences.ResourcesType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Oliver Gierke
 */
@Slf4j
@Component
@RequiredArgsConstructor
class OrdersIntegration {

	static final ResourcesType<Resource<OrderCompleted>> ORDER_COMPLETED = new ResourcesType<Resource<OrderCompleted>>() {};

	private final EventClient events;
	private final Inventory inventory;

	@Scheduled(fixedDelay = 5000)
	public void updateOrders() {

		log.info("Orders integration update triggered…");

		events.doWithIntegration(ORDER_COMPLETED, (order, repository) -> {

			log.info("Processing {} new events…", order.getContent().size());

			order.forEach(resource -> {

				Integration integration = repository.apply(() -> initInventory(resource),
						it -> it.withLastUpdate(resource.getContent().getPublicationDate()));

				log.info("Successful catalog update. New reference time: {}.",
						integration.getLastUpdate().map(it -> it.format(DateTimeFormatter.ISO_DATE_TIME)) //
								.orElseThrow(() -> new IllegalStateException()));
			});
		});
	}

	private void initInventory(Resource<OrderCompleted> resource) {

	}

	interface OrderCompleted {

		LocalDateTime getPublicationDate();

		List<LineItem> getLineItems();

		interface LineItem {

			String getProductId();

			long getAmoung();
		}
	}
}
