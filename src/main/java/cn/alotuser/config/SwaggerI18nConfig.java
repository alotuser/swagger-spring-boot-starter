package cn.alotuser.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import cn.alotus.core.util.StrUtil;
import cn.alotuser.core.SwaggerMessagePlugin;
import cn.alotuser.properties.SwaggerProperties;
import cn.alotuser.util.SwaggerUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.schema.ModelFacets;
import springfox.documentation.schema.ModelSpecification;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelBuilderPlugin;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * * SwaggerI18nConfig is a configuration class that integrates internationalization (i18n) support into Swagger documentation. It uses either a MessageSource or a custom SwaggerMessagePlugin to resolve i18n text for API operations, parameters, and models.
 */
@Configuration
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 1000)
@EnableConfigurationProperties(SwaggerProperties.class)
public class SwaggerI18nConfig implements OperationBuilderPlugin, ModelPropertyBuilderPlugin, ParameterBuilderPlugin, ModelBuilderPlugin {

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

	/**
	 * Resolves the i18n text based on the provided value.
	 *
	 * @param val The value to resolve.
	 * @return The resolved text, or the original value if no translation is found.
	 */
	private String resolveI18nText(String val) {

		try {
			String key = SwaggerUtil.getKey(val);
			if (StrUtil.isNotBlank(key)) {
				if (null != swaggerPlugin) {
					return swaggerPlugin.getMessage(key);
				} else if (null != messageSource) {
					return messageSource.getMessage(key, null, swaggerProperties.getLocale());
				}
			}
		} catch (Exception e) {
			return e.getMessage();
		}
		return val;

	}

}
