/*
 *  Copyright 2014 hbz, Fabian Steeg
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.stream.converter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.stream.converter.JsonEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Add Elasticsearch bulk indexing metadata to JSON input.<br/>
 * Use after {@link JsonEncoder}, before writing.
 * 
 * @author Fabian Steeg (fsteeg)
 *
 */
@In(String.class)
@Out(String.class)
public class JsonToElasticsearchBulk extends 
		DefaultObjectPipe<String, ObjectReceiver<String>> {

	private ObjectMapper mapper = new ObjectMapper();
	private String idKey;
	private String type;
	private String index;

	/**
	 * @param idKey The key of the JSON value to be used as the ID for the record
	 * @param type The Elasticsearch index type
	 * @param index The Elasticsearch index name
	 */
	public JsonToElasticsearchBulk(String idKey, String type, String index) {
		this.idKey = idKey;
		this.type = type;
		this.index = index;
	}

	@Override
	public void process(String obj) {
		StringWriter stringWriter = new StringWriter();
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> json = mapper.readValue(obj, Map.class);
			Map<String, Object> detailsMap = new HashMap<String, Object>();
			Map<String, Object> indexMap = new HashMap<String, Object>();
			indexMap.put("index", detailsMap);
			detailsMap.put("_id", json.get(idKey));
			detailsMap.put("_type", type);
			detailsMap.put("_index", index);
			mapper.writeValue(stringWriter, indexMap);
			stringWriter.write("\n");
			mapper.writeValue(stringWriter, json);
		} catch (IOException e) {
			e.printStackTrace();
		}
		getReceiver().process(stringWriter.toString());
	}
}
