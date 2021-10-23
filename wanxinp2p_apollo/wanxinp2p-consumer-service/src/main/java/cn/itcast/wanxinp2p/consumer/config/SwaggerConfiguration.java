package cn.itcast.wanxinp2p.consumer.config;
//开发过程中用，产品上线要关闭swagger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration //定义配置类,可替换xml配置文件，被注解的类内部包含有一个或多个被@Bean注解的方法
@ConditionalOnProperty(prefix = "swagger",value = {"enable"},havingValue = "true") //开启swagger的开关 从配置文件中获取swagger.enable的值，如果是true开启
@EnableSwagger2  //开启swagger注解支持
public class SwaggerConfiguration {

	@Bean //添加的bean的id为方法名
	public Docket buildDocket() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(buildApiInfo())
				.select()
				// 要扫描的API(Controller)基础包
				.apis(RequestHandlerSelectors.basePackage("cn.itcast.wanxinp2p")) //包扫描 cn.itcast.wanxinp2p凡是这个范围的swagger注解都被扫描
				.paths(PathSelectors.any())
				.build();
	}

	//生成文档的基本内容
	private ApiInfo buildApiInfo() {
		Contact contact = new Contact("黑马程序员","","");
		return new ApiInfoBuilder()
				.title("万信金融P2P平台-用户服务API文档") //标题
				.description("包含用户服务api") //描述信息
				.contact(contact) //作者
				.version("1.0.0").build(); //版本号
	}
}
