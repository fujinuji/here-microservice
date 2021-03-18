package ro.fujinuji.bookaseat.here.microservice.config;

import com.here.account.auth.OAuth1ClientCredentialsProvider;
import com.here.account.http.apache.ApacheHttpClientProvider;
import com.here.account.oauth2.*;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import ro.fujinuji.bookaseat.here.microservice.model.TokenHolder;

import java.io.*;

@Configuration
public class TokenGenerator implements SchedulingConfigurer {

    @Value("${here.credentials.file}")
    private String fileName;
    private final ResourceLoader resourceLoader;
    private Long timeForNextToken;

    public TokenGenerator(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
//    private final TokenHolder tokenHolder;

//    public TokenGenerator(TokenHolder tokenHolder) {
//        this.tokenHolder = tokenHolder;
//    }

    @Bean
    public TokenHolder setupTokenHolder() throws Exception {
        TokenEndpoint tokenEndpoint = HereAccount.getTokenEndpoint(
                ApacheHttpClientProvider.builder().build(),
                credentialsProvider()
        );

        Fresh<AccessTokenResponse> fresh = tokenEndpoint.requestAutoRefreshingToken(new ClientCredentialsGrantRequest());
        String token = fresh.get().getAccessToken();

        timeForNextToken = fresh.get().getExpiresIn();

        TokenHolder tokenHolder = new TokenHolder();
        tokenHolder.setToken(token);

        return tokenHolder;
    }

    private TokenHolder configureToken() throws Exception {
        TokenEndpoint tokenEndpoint = HereAccount.getTokenEndpoint(
                ApacheHttpClientProvider.builder().build(),
                credentialsProvider()
        );

        Fresh<AccessTokenResponse> fresh = tokenEndpoint.requestAutoRefreshingToken(new ClientCredentialsGrantRequest());
        String token = fresh.get().getAccessToken();

        timeForNextToken = fresh.get().getExpiresIn();

        TokenHolder tokenHolder = new TokenHolder();
        tokenHolder.setToken(token);

        return tokenHolder;
    }

    private File getFile() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/" + fileName);
        File file = File.createTempFile("test", ".tmp");

        try(OutputStream outputStream = new FileOutputStream(file)){
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            // handle exception here
        }

        return file;
    }

    private OAuth1ClientCredentialsProvider credentialsProvider() throws Exception {
        return new OAuth1ClientCredentialsProvider.FromFile(getFile());
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {

    }

//    @Override
//    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
//        scheduledTaskRegistrar.addTriggerTask(() -> {
//            try {
//                tokenHolder.setToken(configureToken().getToken());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }, triggerContext -> {
//            Calendar nextExecutionTime = new GregorianCalendar();
//            Date lastActualExecutionTime = triggerContext.lastActualExecutionTime();
//            nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
//            nextExecutionTime.add(Calendar.SECOND, timeForNextToken.intValue());
//            return nextExecutionTime.getTime();
//        });
//    }
}
