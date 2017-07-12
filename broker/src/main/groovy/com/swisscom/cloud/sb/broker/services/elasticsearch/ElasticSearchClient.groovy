package com.swisscom.cloud.sb.broker.services.elasticsearch

import com.mongodb.MongoCredential
import com.mongodb.MongoTimeoutException
import com.mongodb.ServerAddress

import static com.swisscom.cloud.sb.broker.error.ErrorCode.MONGODB_NOT_READY_YET
import static com.swisscom.cloud.sb.broker.util.StringGenerator.randomAlphaNumericOfLength16

class ElasticSearchClient {
    public static final String ADMIN_DATABASE = "admin"

    private final String username
    private final String password
    private final String host
    private final int port
    private final String database

    ElasticSearchClient(String username, String password, String host, int port, String database) {
        this.username = username
        this.password = password
        this.host = host
        this.port = port
        this.database = database
    }
}