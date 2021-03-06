/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.dmn.jpmml;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.api.core.DMNType;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.dmn.core.impl.CompositeTypeImpl;
import org.kie.dmn.core.impl.DMNModelImpl;
import org.kie.dmn.core.impl.SimpleTypeImpl;
import org.kie.dmn.core.pmml.DMNImportPMMLInfo;
import org.kie.dmn.core.pmml.DMNPMMLModelInfo;
import org.kie.dmn.core.util.DMNRuntimeUtil;
import org.kie.dmn.feel.lang.types.BuiltInType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DMNInvokingjPMMLTest {

    public DMNInvokingjPMMLTest() {
        super();
    }

    public static final Logger LOG = LoggerFactory.getLogger(DMNInvokingjPMMLTest.class);

    @Test
    public void testInvokeIris() {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("Invoke Iris.dmn",
                                                                                       DMNInvokingjPMMLTest.class,
                                                                                       "iris model.pmml");
        final DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_91c67ae0-5753-4a23-ac34-1b558a006efd", "http://www.dmg.org/PMML-4_1");
        assertThat( dmnModel, notNullValue() );
        assertThat( DMNRuntimeUtil.formatMessages( dmnModel.getMessages() ), dmnModel.hasErrors(), is( false ) );

        final DMNContext emptyContext = DMNFactory.newContext();

        checkInvokeIris(runtime, dmnModel, emptyContext);
    }

    private void checkInvokeIris(final DMNRuntime runtime, final DMNModel dmnModel, final DMNContext emptyContext) {
        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, emptyContext);
        LOG.debug("{}", dmnResult);
        assertThat(DMNRuntimeUtil.formatMessages(dmnResult.getMessages()), dmnResult.hasErrors(), is(false));

        final DMNContext result = dmnResult.getContext();
        assertThat(result.get("Decision"), is("Iris-versicolor"));
    }

    @Test
    public void testInvokeIris_in1_wrong() {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("Invoke Iris_in1.dmn",
                                                                                       DMNInvokingjPMMLTest.class,
                                                                                       "iris model.pmml");
        final DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_91c67ae0-5753-4a23-ac34-1b558a006efd", "http://www.dmg.org/PMML-4_1");
        assertThat(dmnModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()), dmnModel.hasErrors(), is(false));

        final DMNContext context = DMNFactory.newContext();
        context.set("in1", 99);

        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, context);
        LOG.debug("{}", dmnResult);
        assertThat(DMNRuntimeUtil.formatMessages(dmnResult.getMessages()), dmnResult.hasErrors(), is(true));
        assertTrue(dmnResult.getMessages().stream().anyMatch(m -> m.getSourceId().equals("in1"))); // ... 'in1': the dependency value '99' is not allowed by the declared type (DMNType{ iris : sepal_length })
    }

    @Test
    public void testInvokeIris_in1_ok() {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("Invoke Iris_in1.dmn",
                                                                                       DMNInvokingjPMMLTest.class,
                                                                                       "iris model.pmml");
        final DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_91c67ae0-5753-4a23-ac34-1b558a006efd", "http://www.dmg.org/PMML-4_1");
        assertThat(dmnModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()), dmnModel.hasErrors(), is(false));

        final DMNContext context = DMNFactory.newContext();
        context.set("in1", 4.3);

        checkInvokeIris(runtime, dmnModel, context);
    }

    @Test
    public void testDummyInteger() {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("dummy_integer.dmn",
                                                                                       DMNInvokingjPMMLTest.class,
                                                                                       "dummy_integer.pmml");
        final DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_d9065b95-bc37-41dc-8566-8191af7b7867", "Drawing 1");
        assertThat(dmnModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()), dmnModel.hasErrors(), is(false));

        final DMNContext emptyContext = DMNFactory.newContext();

        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, emptyContext);
        LOG.debug("{}", dmnResult);
        assertThat(DMNRuntimeUtil.formatMessages(dmnResult.getMessages()), dmnResult.hasErrors(), is(false));

        final DMNContext result = dmnResult.getContext();
        assertThat(result.get("hardcoded"), is(new BigDecimal(3)));
    }

    @Test
    public void testMultipleOutputs() {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("invoke_iris_KNN.dmn",
                                                                                       DMNInvokingjPMMLTest.class,
                                                                                       "iris_KNN.pmml");
        final DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_a76cdc83-83b1-4f9c-8cf8-5a0179e776d5", "Drawing 1");
        assertThat(dmnModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()), dmnModel.hasErrors(), is(false));

        final DMNContext emptyContext = DMNFactory.newContext();

        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, emptyContext);
        LOG.debug("{}", dmnResult);
        assertThat(DMNRuntimeUtil.formatMessages(dmnResult.getMessages()), dmnResult.hasErrors(), is(false));

        // > iris[150,]
        //     Sepal.Length Sepal.Width Petal.Length Petal.Width   Species  ID
        // 150          5.9           3          5.1         1.8 virginica 150
        final DMNContext result = dmnResult.getContext();
        Map<String, Object> resultOfHardcoded = (Map<String, Object>) result.get("hardcoded");
        assertThat(resultOfHardcoded.size(), greaterThan(1));
        assertThat(resultOfHardcoded, hasEntry("Predicted_Species", "virginica"));
        assertThat(resultOfHardcoded, hasKey("Predicted_Petal.Width"));
        assertThat((BigDecimal) resultOfHardcoded.get("Predicted_Petal.Width"), is(closeTo(new BigDecimal("1.9333333333333336"), new BigDecimal("0.1"))));
        // no special interest to check the other output fields as the above are the user-facing most interesting ones.

        // additional import info.
        Map<String, DMNImportPMMLInfo> pmmlImportInfo = ((DMNModelImpl) dmnModel).getPmmlImportInfo();
        assertThat(pmmlImportInfo.keySet(), hasSize(1));
        DMNImportPMMLInfo p0 = pmmlImportInfo.values().iterator().next();
        assertThat(p0.getImportName(), is("test20190907"));
        assertThat(p0.getModels(), hasSize(1));
        DMNPMMLModelInfo m0 = p0.getModels().iterator().next();
        assertThat(m0.getName(), is("kNN_model"));

        Map<String, DMNType> outputFields = m0.getOutputFields();
        CompositeTypeImpl output = (CompositeTypeImpl)outputFields.get("kNN_model");
        assertEquals("test20190907", output.getNamespace());

        Map<String, DMNType> fields = output.getFields();
        SimpleTypeImpl out1 = (SimpleTypeImpl)fields.get("Predicted_Species");
        assertEquals("test20190907", out1.getNamespace());
        assertEquals(BuiltInType.STRING, out1.getFeelType());

        SimpleTypeImpl out2 = (SimpleTypeImpl)fields.get("Predicted_Petal.Width");
        assertEquals("test20190907", out2.getNamespace());
        assertEquals(BuiltInType.NUMBER, out2.getFeelType());

        SimpleTypeImpl out3 = (SimpleTypeImpl)fields.get("neighbor1");
        assertEquals("test20190907", out3.getNamespace());
        assertEquals(BuiltInType.STRING, out3.getFeelType());

        SimpleTypeImpl out4 = (SimpleTypeImpl)fields.get("neighbor2");
        assertEquals("test20190907", out4.getNamespace());
        assertEquals(BuiltInType.STRING, out4.getFeelType());

        SimpleTypeImpl out5 = (SimpleTypeImpl)fields.get("neighbor3");
        assertEquals("test20190907", out5.getNamespace());
        assertEquals(BuiltInType.STRING, out5.getFeelType());
    }

    @Test
    public void testMultipleOutputsNoModelName() {
        final DMNRuntime runtime = DMNRuntimeUtil.createRuntimeWithAdditionalResources("invoke_iris_KNN_noModelName.dmn",
                                                                                       DMNInvokingjPMMLTest.class,
                                                                                       "iris_KNN_noModelName.pmml");
        final DMNModel dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_a76cdc83-83b1-4f9c-8cf8-5a0179e776d5", "Drawing 1");
        assertThat(dmnModel, notNullValue());
        assertThat(DMNRuntimeUtil.formatMessages(dmnModel.getMessages()), dmnModel.hasErrors(), is(false));

        final DMNContext emptyContext = DMNFactory.newContext();

        final DMNResult dmnResult = runtime.evaluateAll(dmnModel, emptyContext);
        LOG.debug("{}", dmnResult);
        assertThat(DMNRuntimeUtil.formatMessages(dmnResult.getMessages()), dmnResult.hasErrors(), is(false));

        // > iris[150,]
        //     Sepal.Length Sepal.Width Petal.Length Petal.Width   Species  ID
        // 150          5.9           3          5.1         1.8 virginica 150
        final DMNContext result = dmnResult.getContext();
        Map<String, Object> resultOfHardcoded = (Map<String, Object>) result.get("hardcoded");
        assertThat(resultOfHardcoded.size(), greaterThan(1));
        assertThat(resultOfHardcoded, hasEntry("Predicted_Species", "virginica"));
        assertThat(resultOfHardcoded, hasKey("Predicted_Petal.Width"));
        assertThat((BigDecimal) resultOfHardcoded.get("Predicted_Petal.Width"), is(closeTo(new BigDecimal("1.9333333333333336"), new BigDecimal("0.1"))));
        // no special interest to check the other output fields as the above are the user-facing most interesting ones.

        // additional import info.
        Map<String, DMNImportPMMLInfo> pmmlImportInfo = ((DMNModelImpl) dmnModel).getPmmlImportInfo();
        assertThat(pmmlImportInfo.keySet(), hasSize(1));
        DMNImportPMMLInfo p0 = pmmlImportInfo.values().iterator().next();
        assertThat(p0.getImportName(), is("test20190907"));
        assertThat(p0.getModels(), hasSize(1));
        DMNPMMLModelInfo m0 = p0.getModels().iterator().next();
        assertNull(m0.getName());

        Map<String, DMNType> outputFields = m0.getOutputFields();

        SimpleTypeImpl out1 = (SimpleTypeImpl)outputFields.get("Predicted_Species");
        assertEquals(BuiltInType.UNKNOWN, out1.getFeelType());

        SimpleTypeImpl out2 = (SimpleTypeImpl)outputFields.get("Predicted_Petal.Width");
        assertEquals(BuiltInType.UNKNOWN, out2.getFeelType());

        SimpleTypeImpl out3 = (SimpleTypeImpl)outputFields.get("neighbor1");
        assertEquals(BuiltInType.UNKNOWN, out3.getFeelType());

        SimpleTypeImpl out4 = (SimpleTypeImpl)outputFields.get("neighbor2");
        assertEquals(BuiltInType.UNKNOWN, out4.getFeelType());

        SimpleTypeImpl out5 = (SimpleTypeImpl)outputFields.get("neighbor3");
        assertEquals(BuiltInType.UNKNOWN, out5.getFeelType());
    }
}
