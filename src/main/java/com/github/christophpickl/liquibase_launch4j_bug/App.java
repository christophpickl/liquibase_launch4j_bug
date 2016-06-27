package com.github.christophpickl.liquibase_launch4j_bug;

public class App {

    public static void main(String[] args) {
        System.out.println("App START");

        /*
        val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(dataSource.connection))
        log.trace("liquibase database product: {}, version: {}, short name: {}", database.databaseProductName, database.databaseProductVersion, database.shortName)

        val liquibase = Liquibase(CHANGELOG, ClassLoaderResourceAccessor(), database)
        liquibase.update(Contexts(), LabelExpression())
         */
        System.out.println("App END");
    }
}
