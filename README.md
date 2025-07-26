# swagger-spring-boot-starter 多语言支持

## 简介  
`swagger-spring-boot-starter` 是一个用于 Spring Boot 项目的 Swagger 文档自动生成组件，内置国际化（i18n）能力，支持多语言 API 文档展示，助力全球化项目开发。

---

## 特性  
- 支持接口、参数、模型等文档内容的多语言自动切换  
- 可集成 Spring 的 `MessageSource` 或自定义国际化插件  
- 兼容 Swagger2/Springfox，零侵入式接入  
- 配置简单，支持多语言资源文件  
- 支持动态切换语言，适配多地区用户  

---

## 快速开始  

### 1\. 添加依赖  
``` xml
<dependency>
    <groupId>com.github.alotuser</groupId>
    <artifactId>swagger-spring-boot-starter</artifactId>
    <version>1.1.1</version>
</dependency>
```


### 2\. 配置国际化资源  
在 `src/main/resources/i18n` 下添加多语言 `messages_xx.properties` 文件，例如：  
- `messages_zh_CN.properties`  
- `messages_en.properties`
- `messages_fr_FR.properties`  

内容示例：  
swagger.api.title=接口标题  
swagger.api.desc=接口描述



### 3\. 启用国际化配置  
在 `application.yml` 中设置默认语言：  

```yml
swagger:
  locale: en
```


---

## 注解使用示例

### 1\. `@ApiModel` 用于实体类说明  
```java
@ApiModel(value = "SysUser", description = "#{api.sysUser} 系统用户实体类")
public class SysUser {

@ApiModelProperty(value = "#{api.sysUser.id} 用户ID", example = "1")
private Long id;

@ApiModelProperty(value = "#{api.sysUser.name} 姓名", example = "张三")
private String name;

@ApiModelProperty(value = "#{api.sysUser.age} 年龄", example = "18")
private Integer age;

@ApiModelProperty(value = "#{api.sysUser.email} 邮箱", example = "test@alotu.com")
private String email;

// getter/setter ...

}
```


### 2\. `@ApiParam` 用于接口参数说明  
```java
@PostMapping("/getSysUser")
public List getSysUser(
@ApiParam(value = "#{api.current} 当前页", example = "1") @RequestParam Long current,
@ApiParam(value = "#{api.size} 每页显示条数", example = "10") @RequestParam Long size,
@ApiParam(value = "#{api.sysUser.name} 姓名", example = "张三") @RequestParam(required = false) String name,
@ApiParam(value = "#{api.sysUser.age} 年龄", example = "18") @RequestParam(required = false) Integer age ) {
// …
}
```


### 3\. `@ApiModelProperty` 用于实体字段说明  
见上方 `SysUser` 示例。

---

通过以上注解，结合国际化 key，可实现多语言的 API 文档自动切换。

---

## 进阶用法  
- 支持自定义 `SwaggerMessagePlugin` 实现更灵活的国际化  
- 可动态切换语言，适配多地区用户  
```java
@Component
public class CustomPlugin implements SwaggerMessagePlugin {

	@Autowired
	private SwaggerProperties  swaggerProperties;
 
	@Override
	public String getMessage(String code) {
		
		//自定义语言
		String msg=null;
		try {
			String json = "{\"api\":{\"hello\":{\"value\":\"自定义的"+swaggerProperties.getLocale().toString()+" App Controller\"}}}";
			
			msg = (String)JSONUtil.getByPath(JSONUtil.parse(json), code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return msg;
	}

}
```
---

## 示例代码  
详见 `https://github.com/alotuser/swagger-demo`，包含多语言集成核心逻辑。

---

## 反馈与支持  
如需更多帮助或定制功能，请提交 Issue 或联系维护者。

