/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.quarkus.component.solr.it;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import io.quarkus.runtime.StartupEvent;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.solr.SolrConstants;
import org.apache.camel.quarkus.component.solr.it.bean.Item;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;

public abstract class SolrCommonResource {

    @Inject
    ProducerTemplate producerTemplate;

    /**
     * solr camel component URI
     */
    String solrComponentURI;

    /**
     * used to check data
     */
    HttpSolrClient solrClient;

    /**
     * inits the params solrComponentURI and solrClient
     * 
     * @param startupEvent
     */
    public abstract void init(StartupEvent startupEvent);

    @PUT
    @Path("bean")
    public Response addBean(Item bean) {
        producerTemplate.sendBodyAndHeader(solrComponentURI, bean, SolrConstants.OPERATION, SolrConstants.OPERATION_ADD_BEAN);
        solrCommit();
        return Response.accepted().build();
    }

    @PUT
    @Path("beans")
    public Response addBeans(List<Item> beans) {
        producerTemplate.sendBodyAndHeader(solrComponentURI, beans, SolrConstants.OPERATION, SolrConstants.OPERATION_ADD_BEANS);
        solrCommit();
        return Response.accepted().build();
    }

    @DELETE
    @Path("bean")
    public Response deleteBeanById(String id) {
        producerTemplate.sendBodyAndHeader(solrComponentURI, id, SolrConstants.OPERATION, SolrConstants.OPERATION_DELETE_BY_ID);
        solrCommit();
        return Response.accepted().build();
    }

    @DELETE
    @Path("beans")
    public Response deleteByIdPrefix(String idPrefix) {
        producerTemplate.sendBodyAndHeader(solrComponentURI, String.format("id:%s*", idPrefix), SolrConstants.OPERATION,
                SolrConstants.OPERATION_DELETE_BY_QUERY);
        solrCommit();
        return Response.accepted().build();
    }

    @PUT
    @Path("document/commit")
    public Response insertAndCommit(Map<String, Object> fields) {
        solrInsert(fields);
        solrCommit();
        return Response.accepted().build();
    }

    @PUT
    @Path("document")
    public Response insertDocument(Map<String, Object> fields) {
        solrInsert(fields);
        return Response.accepted().build();
    }

    private void solrInsert(Map<String, Object> fields) {
        String docAsXml = createDocument(fields);
        producerTemplate.sendBodyAndHeader(solrComponentURI, docAsXml, SolrConstants.OPERATION, SolrConstants.OPERATION_INSERT);
    }

    private String createDocument(Map<String, Object> fields) {
        SolrInputDocument doc = new SolrInputDocument();
        fields.forEach((key, value) -> doc.addField(key, value));
        return ClientUtils.toXML(doc);
    }

    @GET
    @Path("optimize")
    public Response optimize() {
        producerTemplate.sendBodyAndHeader(solrComponentURI, null, SolrConstants.OPERATION, SolrConstants.OPERATION_OPTIMIZE);
        return Response.accepted().build();
    }

    @GET
    @Path("rollback")
    public Response rollback() {
        producerTemplate.sendBodyAndHeader(solrComponentURI, null, SolrConstants.OPERATION, SolrConstants.OPERATION_ROLLBACK);
        return Response.accepted().build();
    }

    @GET
    @Path("commit")
    public Response commit() {
        solrCommit();
        return Response.accepted().build();
    }

    @GET
    @Path("softcommit")
    public Response softcommit() {
        producerTemplate.sendBodyAndHeader(solrComponentURI, null, SolrConstants.OPERATION,
                SolrConstants.OPERATION_SOFT_COMMIT);
        return Response.accepted().build();
    }

    @PUT
    @Path("streaming")
    public Response insertStreaming(Map<String, Object> fields) {
        String docAsXml = createDocument(fields);
        producerTemplate.sendBodyAndHeader(solrComponentURI, docAsXml, SolrConstants.OPERATION,
                SolrConstants.OPERATION_INSERT_STREAMING);
        return Response.accepted().build();
    }

    private void solrCommit() {
        producerTemplate.sendBodyAndHeader(solrComponentURI, null, SolrConstants.OPERATION, SolrConstants.OPERATION_COMMIT);
    }

    @GET
    @Path("bean/{id}")
    public Item getBeanById(@PathParam("id") String id) throws IOException, SolrServerException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("q", "id:" + id);
        QueryRequest queryRequest = new QueryRequest(solrQuery);
        QueryResponse response = queryRequest.process(solrClient);
        List<Item> responses = response.getBeans(Item.class);
        return responses.size() != 0 ? responses.get(0) : new Item();
    }
}
