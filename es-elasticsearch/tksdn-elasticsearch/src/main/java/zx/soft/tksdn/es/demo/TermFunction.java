package zx.soft.tksdn.es.demo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.action.termvectors.MultiTermVectorsItemResponse;
import org.elasticsearch.action.termvectors.MultiTermVectorsRequestBuilder;
import org.elasticsearch.action.termvectors.MultiTermVectorsResponse;
import org.elasticsearch.action.termvectors.TermVectorsRequest;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;

import zx.soft.tksdn.es.utils.ESTransportClient;

public class TermFunction {

	public static void main(String[] args) throws IOException {
		Client client = ESTransportClient.getClient();

		TermVectorsRequest.FilterSettings filterSettings = new TermVectorsRequest.FilterSettings(null, null, null, null,
				null, 2, null);
		TermVectorsResponse resp = client.prepareTermVectors("tekuan", "record", "76BDE769FE74D778FE7AFAD1D6273BB7")
				.setPositions(false).setPositions(false).setOffsets(false).setSelectedFields("content")
				.setFieldStatistics(false).setFilterSettings(filterSettings).get();
		MultiTermVectorsRequestBuilder mBuilder = client.prepareMultiTermVectors().add("tekuan", "record", "");
		TermFunction test = new TermFunction();
		XContentBuilder builder;
		try {
			builder = XContentFactory.jsonBuilder().startObject();
			resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
			builder.endObject();
			System.out.println(builder.string());
			Map<String, Object> map = XContentHelper.convertToMap(builder.bytes(), false).v2();
			System.out.println(map);
		} catch (IOException e) {

		}
	}

	/**
	 * Prints term-vectors for child documents given their parent ids
	 *
	 * @param client    Es client
	 * @param index     Index name
	 * @param postIDs   Map of child docuemnt ID to its _parent/_routing ID
	 * @return
	 * @throws IOException
	 */
	public static void builtTermVectorRequest(Client client, String index, Map<String, String> postIDs)
			throws IOException {
		/**
		 * Initialize the MultiTermVectorsRequestBuilder first
		 */
		MultiTermVectorsRequestBuilder multiTermVectorsRequestBuilder = client.prepareMultiTermVectors();
		TermVectorsRequest.FilterSettings filterSettings = new TermVectorsRequest.FilterSettings(null, null, null, null,
				null, 2, null);
		/**
		 * For every document ID, create a different TermVectorsRequest and
		 * add it to the MultiTermVectorsRequestBuilder created above
		 */
		for (Map.Entry<String, String> entry : postIDs.entrySet()) {
			String currentPostId = entry.getKey();
			String currentRoutingID = entry.getValue();
			TermVectorsRequest termVectorsRequest = new TermVectorsRequest().index(index).type("doc_type")
					.id(currentPostId).offsets(false).positions(false).fieldStatistics(false).selectedFields("content")
					.filterSettings(filterSettings);
			//					.index(index).type("doc_type")
			//					.id(currentPostId).parent(currentRoutingID) // You can use .routing(currentRoutingID) also
			//					.selectedFields("some_field").termStatistics(true);
			multiTermVectorsRequestBuilder.add(termVectorsRequest);
		}

		/**
		 * Finally execute the MultiTermVectorsRequestBuilder
		 */
		MultiTermVectorsResponse response = multiTermVectorsRequestBuilder.execute().actionGet();

		List<String> termStrings = new ArrayList<>();
		for (MultiTermVectorsItemResponse res : response) {
			Fields fields = res.getResponse().getFields();
			Iterator<String> iterator = fields.iterator();
			while (iterator.hasNext()) {
				String field = iterator.next();
				Terms terms = fields.terms(field);
				TermsEnum termsEnum = terms.iterator();
				//			termsEnum.postings();
				while (termsEnum.next() != null) {
					BytesRef term = termsEnum.term();
					if (term != null) {
						termStrings.add(term.utf8ToString());
					}
				}
			}
		}
		XContentBuilder builder;
		try {
			builder = XContentFactory.jsonBuilder().startObject();
			response.toXContent(builder, ToXContent.EMPTY_PARAMS);
			builder.endObject();
			System.out.println(builder.prettyPrint().string());
		} catch (IOException e) {
		}
	}
}