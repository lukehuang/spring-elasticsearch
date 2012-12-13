/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.pilato.spring.elasticsearch.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import fr.pilato.spring.elasticsearch.ElasticsearchClientFactoryBean;
import fr.pilato.spring.elasticsearch.ElasticsearchTransportClientFactoryBean;

public class ClientBeanDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		GenericBeanDefinition bdef = new GenericBeanDefinition();
		
		// When node is not null, we should build a client.
		// When node is null, we want to build a transport client.
		
		String id = element.getAttribute("id");
		String name = element.getAttribute("name");

		String node = element.getAttribute("node");
		String esNodes = element.getAttribute("esNodes");
		
		String settingsFile = element.getAttribute("settingsFile");
		
		// Checking bean definition
		boolean isClientNode = (node != null && node.length() > 0);
		boolean isEsNodesEmpty = (esNodes == null || esNodes.length() == 0);
		
		if (isClientNode && !isEsNodesEmpty) {
			throw new RuntimeException("Incorrect settings. You should not set esNodes when using a client node.");
		}
		
		if (!isClientNode && isEsNodesEmpty) {
			throw new RuntimeException("Incorrect settings. You must set esNodes when creating a transport client.");
		}
		
		BeanDefinition client = null;
		
		if (isClientNode) {
			bdef.setBeanClass(ElasticsearchClientFactoryBean.class);
			client = ClientBeanDefinitionParser.buildClientDef(node, settingsFile);
		} else {
			bdef.setBeanClass(ElasticsearchTransportClientFactoryBean.class);
			client = ClientBeanDefinitionParser.buildTransportClientDef(esNodes, settingsFile);
		}
		

		// Register NodeBeanDefinition
		if (id != null && id.length() > 0) {
			parserContext.getRegistry().registerBeanDefinition(id, client);	
		} else {
			parserContext.getRegistry().registerBeanDefinition(name, client);
		}
		
		return bdef;
	}

	public static BeanDefinition buildClientDef(String node, String settingsFile) {
		BeanDefinitionBuilder nodeFactory = BeanDefinitionBuilder.rootBeanDefinition(ElasticsearchClientFactoryBean.class);
		if (settingsFile != null && settingsFile.length() > 0) {
			nodeFactory.addPropertyValue("settingsFile", settingsFile);	
		} 
		if (node != null && node.length() > 0) {
			nodeFactory.addPropertyReference("node", node);	
		} 
		return nodeFactory.getBeanDefinition();
	}

	public static BeanDefinition buildTransportClientDef(String esNodes, String settingsFile) {
		BeanDefinitionBuilder nodeFactory = BeanDefinitionBuilder.rootBeanDefinition(ElasticsearchTransportClientFactoryBean.class);
		if (settingsFile != null && settingsFile.length() > 0) {
			nodeFactory.addPropertyValue("settingsFile", settingsFile);	
		} 
		if (esNodes != null && esNodes.length() > 0) {
			nodeFactory.addPropertyValue("esNodes", esNodes);	
		} 
		return nodeFactory.getBeanDefinition();
	}

}
