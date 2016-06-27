package com.github.christophpickl.liquibase_launch4j_bug;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.lockservice.LockServiceFactory;
import liquibase.lockservice.StandardLockService;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import org.hsqldb.jdbc.JDBCDataSource;

public class App {

    public static void main(String[] args) throws Exception {
        try {
            log("App START");
            log("=========================");

            List<String> packages = ServiceLocator.getInstance().getPackages();
            log("Packages to scan: " + packages.size()); // IDE: 17, EXE: 17 (so they are the same, something else must be wrong here)
            for (String packagee : packages) {
                log("Liquibase package scan: " + packagee);
            }

            log("Connecting to HSQLDB ...");
            JDBCDataSource dataSource = new JDBCDataSource();
            dataSource.setUrl("jdbc:hsqldb:mem:mymemdb");
            dataSource.setUser("SA");
            log("Connecting to HSQLDB ... DONE");


            if (DatabaseFactory.getInstance().getDatabase("hsqldb") == null) {
                log("Registering HSQLDB manually.");
                HsqlDatabase hsqlDatabase = new HsqlDatabase();
                DatabaseFactory.getInstance().register(hsqlDatabase);
                LockServiceFactory.getInstance().register(new StandardLockService());
            }

            log("Establishing liquibase connection ...");
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()));

            // when executing in IDE or as fatJar:
            // "database product: HSQL Database Engine, version: 2.3.4, shortname: hsqldb"

            // when executed as an EXE wrapped by launch4j:
            // "database product: HSQL Database Engine, version: 2.3.4, shortname: unsupported"

            log(String.format("database product: %s, version: %s, shortname: %s (%s)",
                database.getDatabaseProductName(), database.getDatabaseProductVersion(), database.getShortName(), database.getClass().getName()));
            log("Establishing liquibase connection ... DONE");

            List<Database> implementedDatabases = DatabaseFactory.getInstance().getImplementedDatabases();
            log("Number of implemented databases: " + implementedDatabases.size());
            for (Database db : implementedDatabases) {
                log("Registered implemented database: " + db.getClass().getSimpleName());
                /*
                in IDE returns:
                Number of implemented databases: 15
                Registered implemented database: SybaseDatabase
                Registered implemented database: FirebirdDatabase
                Registered implemented database: OracleDatabase
                Registered implemented database: SQLiteDatabase
                Registered implemented database: HsqlDatabase
                Registered implemented database: H2Database
                Registered implemented database: MariaDBDatabase
                Registered implemented database: InformixDatabase
                Registered implemented database: UnsupportedDatabase
                Registered implemented database: PostgresDatabase
                Registered implemented database: DB2Database
                Registered implemented database: SybaseASADatabase
                Registered implemented database: DerbyDatabase
                Registered implemented database: MySQLDatabase
                Registered implemented database: MSSQLDatabase

                as EXE returns:
                Number of implemented databases: 0
                */
            }

            Liquibase liquibase = new Liquibase("liquibase_changelog.sql", new ClassLoaderResourceAccessor(), database);
            log("Migrating database via liquibase ...");
            liquibase.update(new Contexts(), new LabelExpression());
            log("Migrating database via liquibase ... DONE");

            log("Closing database connection ...");
            dataSource.getConnection().close();
            log("Closing database connection ... DONE");

            log("=========================");
            log("App END");
        } finally {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("log.log"))) {
                for (String log : logBuffer) {
                    writer.write(log + "\n");
                }
            }
        }
    }

    private static final List<String> logBuffer = new LinkedList<>();
    private static void log(String message) {
        System.out.println(message);
        logBuffer.add(message);
    }

}
