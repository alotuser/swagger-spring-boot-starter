package cn.alotuser.config;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static springfox.documentation.service.Tags.emptyTags;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

import cn.alotus.core.util.StrUtil;
import cn.alotuser.core.SwaggerMessagePlugin;
import cn.alotuser.properties.SwaggerProperties;
import cn.alotuser.util.SwaggerUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.builders.BuilderDefaults;
import springfox.documentation.schema.ModelFacets;
import springfox.documentation.schema.ModelSpecification;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelBuilderPlugin;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.spi.service.ApiListingBuilderPlugin;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ApiListingContext;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;
import springfox.documentation.swagger.web.SwaggerApiListingReader;

/**
 * * SwaggerI18nConfig is a configuration class that integrates internationalization (i18n) support into Swagger documentation. It uses either a MessageSource or a custom SwaggerMessagePlugin to resolve i18n text for API operations, parameters, and models.
 */
@Configuration
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 1000)
@EnableConfigurationProperties(SwaggerProperties.class)
public class SwaggerI18nConfig implements OperationBuilderPlugin, ModelPropertyBuilderPlugin, ParameterBuilderPlugin, ModelBuilderPlugin, ApiListingBuilderPlugin {

	@Autowired(required = false)
	private MessageSource messageSource;

	@Autowired(required = false)
	private SwaggerMessagePlugin swaggerPlugin;

	@Autowired
	private SwaggerProperties swaggerProperties;

	@Override
	public boolean supports(DocumentationType delimiter) {

		return messageSource != null || swaggerPlugin != null;

	}

	@ConditionalOnProperty(prefix = "spring.main", name = "allow-bean-definition-overriding", havingValue = "true", matchIfMissing = false)
	@Bean(name = "swaggerApiListingReader")
	@Primary
	SwaggerApiListingReader swaggerApiListingReader() {
		return new SwaggerApiListingReader() {
			@SuppressWarnings("deprecation")
			@Override
			public void apply(ApiListingContext apiListingContext) {
				Optional<? extends Class<?>> controller = apiListingContext.getResourceGroup().getControllerClass();
				if (controller.isPresent()) {
					Optional<Api> apiAnnotation = ofNullable(findAnnotation(controller.get(), Api.class));
					String description = apiAnnotation.map(Api::description).map(BuilderDefaults::emptyToNull).orElse(null);

					if (StrUtil.isBlank(description)) {
						description = apiAnnotation.map(Api::value).map(BuilderDefaults::emptyToNull).orElse(null);
					}

					// Check if the description starts with the Swagger prefix
					if (StrUtil.startWith(description, SwaggerUtil.SWAGGER_PRE)) {
						description = resolveI18nText(description);
					}

					// If no OAS tags are present, fallback to Api annotation tags or resource group name
					Set<Tag> oasTags = tagsFromOasAnnotations(controller.get());

					Set<String> tagSet = new TreeSet<>();
					if (oasTags.isEmpty()) {
						Set<String> ts = apiAnnotation.map(tags()).orElse(new TreeSet<>());
						
						ts.forEach(t -> {
							tagSet.add(resolveI18nText(t));
						});
						
						
						if (tagSet.isEmpty()) {
							tagSet.add(apiListingContext.getResourceGroup().getGroupName());
						}
					} else {
						oasTags = oasTags.stream().map(x -> new Tag(resolveI18nText(x.getName()), resolveI18nText(x.getDescription()))).collect(Collectors.toSet());
					}
					apiListingContext.apiListingBuilder().description(description).tagNames(tagSet).tags(oasTags);

				}
			}
		};
	}
	
	
	
	@Override
	public void apply(OperationContext context) {

		String value = context.findAnnotation(ApiOperation.class).map(ApiOperation::value).orElse(SwaggerUtil.SWAGGER_EMPTY);
		if (value.startsWith(SwaggerUtil.SWAGGER_PRE)) {
			String translated = resolveI18nText(value);
			context.operationBuilder().summary(translated);

		}
		String notes = context.findAnnotation(ApiOperation.class).map(ApiOperation::notes).orElse(SwaggerUtil.SWAGGER_EMPTY);
		if (notes.startsWith(SwaggerUtil.SWAGGER_PRE)) {
			String translated = resolveI18nText(notes);
			context.operationBuilder().notes(translated);
		}

	}

	@Override
	public void apply(ParameterContext context) {
		context.resolvedMethodParameter().findAnnotation(ApiParam.class).ifPresent(apiParam -> {
			String desc = resolveI18nText(apiParam.value());
			context.requestParameterBuilder().description(desc);
		});
	}

	@Override
	public void apply(ModelContext context) {

		ModelSpecification modelSpecification = context.getModelSpecificationBuilder().build();

		if (null != modelSpecification) {
			Optional<ModelFacets> facets = modelSpecification.getFacets();
			String val = facets.map(ModelFacets::getDescription).orElse(SwaggerUtil.SWAGGER_EMPTY);
			if (StrUtil.isNotBlank(val)) {
				String desc = resolveI18nText(val);
				// context.getBuilder().description(desc);
				context.getModelSpecificationBuilder().facets(x -> {
					x.description(desc);
				});
			}
		}

	}

	@Override
	public void apply(ModelPropertyContext context) {

		String val = context.getSpecificationBuilder().build().getDescription();
		if (StrUtil.isNotBlank(val)) {
			String desc = resolveI18nText(val);
			context.getSpecificationBuilder().description(desc);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void apply(ApiListingContext apiListingContext) {

		Optional<? extends Class<?>> controller = apiListingContext.getResourceGroup().getControllerClass();
		if (controller.isPresent()) {
			Optional<Api> apiAnnotation = ofNullable(findAnnotation(controller.get(), Api.class));
			String description = apiAnnotation.map(Api::description).map(BuilderDefaults::emptyToNull).orElse(null);
			
			if (StrUtil.isBlank(description)) {
				description = apiAnnotation.map(Api::value).map(BuilderDefaults::emptyToNull).orElse(null);
			}
			
			// Check if the description starts with the Swagger prefix
			if (StrUtil.startWith(description, SwaggerUtil.SWAGGER_PRE)) {
				description = resolveI18nText(description);
			}

			// If no OAS tags are present, fallback to Api annotation tags or resource group name
//			Set<Tag> oasTags = tagsFromOasAnnotations(controller.get());
//
//			Set<String> tagSet = new TreeSet<>();
//			if (oasTags.isEmpty()) {
//				Set<String> ts = apiAnnotation.map(tags()).orElse(new TreeSet<>());
//				ts.forEach(t -> {
//					tagSet.add(resolveI18nText(t));
//				});
//				if (tagSet.isEmpty()) {
//					tagSet.add(apiListingContext.getResourceGroup().getGroupName());
//				}
//			} else {
//				oasTags=oasTags.stream().map(x -> new Tag(x.getName(), resolveI18nText(x.getDescription()))).collect(Collectors.toSet()); 
//			}
			apiListingContext.apiListingBuilder().description(description);
		}
	}

	private Set<Tag> tagsFromOasAnnotations(Class<?> controller) {
		HashSet<Tag> controllerTags = new HashSet<>();
		io.swagger.v3.oas.annotations.tags.Tags tags = findAnnotation(controller, io.swagger.v3.oas.annotations.tags.Tags.class);
		if (tags != null) {
			Arrays.stream(tags.value()).forEach(t -> controllerTags.add(new Tag(t.name(), t.description())));
		}
		io.swagger.v3.oas.annotations.tags.Tag tag = findAnnotation(controller, io.swagger.v3.oas.annotations.tags.Tag.class);
		if (tag != null) {
			controllerTags.add(new Tag(tag.name(), tag.description()));
		}
		return controllerTags;
	}

	private Function<Api, Set<String>> tags() {
		return input -> Stream.of(input.tags()).filter(emptyTags()).collect(toCollection(TreeSet::new));
	}

	/**
	 * Resolves the i18n text based on the provided value.
	 *
	 * @param val The value to resolve.
	 * @return The resolved text, or the original value if no translation is found.
	 */
	private String resolveI18nText(String val) {

		String message = StrUtil.EMPTY;
		try {
			String key = SwaggerUtil.getKey(val);
			if (StrUtil.isNotBlank(key)) {
				if (null != swaggerPlugin) {
					message = swaggerPlugin.getMessage(key);
				}
				if (StrUtil.isEmpty(message) && null != messageSource) {
					message = messageSource.getMessage(key, null, swaggerProperties.getLocale());
				}
				if (StrUtil.isEmpty(message)) {
					message = val;
				}
			} else {
				message = val;
			}
		} catch (Exception e) {
			return e.getMessage();
		}
		return message;

	}


}
