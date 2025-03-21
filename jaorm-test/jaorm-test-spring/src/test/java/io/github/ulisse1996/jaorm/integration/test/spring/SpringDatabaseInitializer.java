package io.github.ulisse1996.jaorm.integration.test.spring;

import com.zaxxer.hikari.HikariDataSource;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;
import io.github.ulisse1996.jaorm.integration.test.DatabaseInitializer;
import io.github.ulisse1996.jaorm.spi.common.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.net.URL;
import java.util.Objects;

public class SpringDatabaseInitializer implements DatabaseInitializer {

    public static final Singleton<PostgreSQLContainer<?>> INSTANCE = Singleton.instance();
    private static final Logger logger = LoggerFactory.getLogger(SpringDatabaseInitializer.class);

    @Override
    public void initDatabase() {
        logger.info("Starting Postgre");
        ensureInit();
        INSTANCE.get().start();
        DataSourceProvider.getCurrent();
    }

    @Override
    public void destroyDatabase() {
        logger.info("Stopping Postgre");
        ensureInit();
        DataSource dataSource = DataSourceProvider.getCurrent().getDataSource();
        TransactionAwareDataSourceProxy proxy = (TransactionAwareDataSourceProxy) dataSource;
        HikariDataSource d = (HikariDataSource) proxy.getTargetDataSource();
        Objects.requireNonNull(d).close();
        INSTANCE.get().stop();
    }

    @Override
    public JdbcDatabaseContainer<?> getContainer() {
        ensureInit();
        return INSTANCE.get();
    }

    @Override
    public URL getSqlFileURL() {
        return SpringDatabaseInitializer.class.getResource("/init.sql");
    }

    private void ensureInit() {
        synchronized (this) {
            if (!INSTANCE.isPresent()) {
                INSTANCE.set(new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.4")));
            }
        }
    }
}
