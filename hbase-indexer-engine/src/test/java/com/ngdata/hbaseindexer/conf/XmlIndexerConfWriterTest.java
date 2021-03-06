/*
 * Copyright 2013 NGDATA nv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ngdata.hbaseindexer.conf;

import com.google.common.collect.Maps;
import com.ngdata.hbaseindexer.ConfigureUtil;
import com.ngdata.hbaseindexer.parse.DefaultResultToSolrMapper;
import com.ngdata.hbaseindexer.uniquekey.StringUniqueKeyFormatter;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Map;

public class XmlIndexerConfWriterTest {
    @Test
    public void testWrite() throws Exception {
        Map<String, String> params = Maps.newHashMap();
        params.put("thename", "thevalue");
        IndexerConf conf = new IndexerConfBuilder()
                .table("the-table")
                .mappingType(IndexerConf.MappingType.COLUMN)
                .rowReadMode(IndexerConf.RowReadMode.DYNAMIC)
                .uniqueyKeyField("kyefield")
                .rowField("rf")
                .columnFamilyField("cf-field")
                .tableNameField("tn-field")
                .globalParams(ConfigureUtil.mapToJson(params))
                .mapperClass(DefaultResultToSolrMapper.class)
                .uniqueKeyFormatterClass(StringUniqueKeyFormatter.class)
                .addFieldDefinition("fieldname", "fieldvalue", FieldDefinition.ValueSource.VALUE, "fieldtype", params)
                .addDocumentExtractDefinition("theprefix", "valueexpr", FieldDefinition.ValueSource.VALUE, "deftype", params)
                .build();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XmlIndexerConfWriter.writeConf(conf,os);

        String xmlString = os.toString();

        XmlIndexerConfReader reader = new XmlIndexerConfReader();
        try {
            reader.validate(IOUtils.toInputStream(xmlString));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Xml is not valid");
        }
        IndexerConf conf2 = reader.read(IOUtils.toInputStream(xmlString));

        Assert.assertEquals(conf.getTable(),conf2.getTable());
        Assert.assertEquals(conf.getMappingType(),conf2.getMappingType());
        Assert.assertEquals(conf.getRowReadMode(),conf2.getRowReadMode());
        Assert.assertEquals(conf.getUniqueKeyField(),conf2.getUniqueKeyField());
        Assert.assertEquals(conf.getRowField(),conf2.getRowField());
        Assert.assertEquals(conf.getColumnFamilyField(),conf2.getColumnFamilyField());
        Assert.assertEquals(conf.getTableNameField(),conf2.getTableNameField());
        Assert.assertTrue(Arrays.equals(conf.getGlobalConfig(), conf2.getGlobalConfig()));
        Assert.assertEquals(conf.getMapperClass(),conf2.getMapperClass());
        Assert.assertEquals(conf.getUniqueKeyFormatterClass(),conf2.getUniqueKeyFormatterClass());
        Assert.assertEquals(conf.getFieldDefinitions().size(),conf2.getFieldDefinitions().size());
        Assert.assertEquals(conf.getDocumentExtractDefinitions().size(),conf2.getDocumentExtractDefinitions().size());
    }

}
