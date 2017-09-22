package example.sos.messaging.orders.integration;

import example.sos.messaging.orders.Order.OrderCompleted;
import example.sos.messaging.orders.ProductInfo;
import example.sos.messaging.orders.ProductInfo.ProductNumber;
import example.sos.messaging.orders.ProductInfoRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.web.JsonPath;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
class KafkaIntegration {

	private final @NonNull ProductInfoRepository productInfos;
	private final @NonNull ProjectionFactory projectionFactory;

	private final @NonNull KafkaOperations<Object, Object> kafka;
	private final @NonNull ObjectMapper mapper;

	@TransactionalEventListener
	void on(OrderCompleted event) throws Exception {
		kafka.send("orders", mapper.writeValueAsString(event));
	}

	@KafkaListener(topics = "products")
	void on(String message) {

		ProductAdded event = readFromMessage(message, ProductAdded.class);
		UUID productNumber = event.getProductNumber();

		productInfos.save(ProductInfo.of(ProductNumber.of(productNumber), event.getDescription(), event.getPrice()));
	}

	private <T> T readFromMessage(String message, Class<T> type) {

		try (InputStream stream = new ByteArrayInputStream(message.getBytes())) {
			return projectionFactory.createProjection(type, stream);
		} catch (IOException o_O) {
			throw new RuntimeException(o_O);
		}
	}

	interface ProductAdded {

		@JsonPath("$.product.id")
		UUID getProductNumber();

		@JsonPath("$.product.name")
		String getDescription();

		@JsonPath("$.product.price")
		BigDecimal getPrice();
	}
}
