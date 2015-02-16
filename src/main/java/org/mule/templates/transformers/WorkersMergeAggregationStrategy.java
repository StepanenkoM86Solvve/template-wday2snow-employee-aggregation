/**
 * Mule Anypoint Template
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.transformers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mule.DefaultMuleEvent;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.routing.AggregationContext;
import org.mule.routing.AggregationStrategy;
import org.mule.streaming.ConsumerIterator;

import com.google.common.collect.Lists;

public class WorkersMergeAggregationStrategy implements AggregationStrategy {

	@Override
	public MuleEvent aggregate(AggregationContext context) throws MuleException {

		List<MuleEvent> muleEventsWithoutException = context.collectEventsWithoutExceptions();
		int muleEventsWithoutExceptionCount = muleEventsWithoutException.size();
		
		if (muleEventsWithoutExceptionCount != 2) {
			throw new IllegalArgumentException("There have to be exactly 2 sources (A and B).");
		}
		
		// mule event that will be rewritten
		MuleEvent originalEvent = context.getOriginalEvent();
		// message which payload will be rewritten
		MuleMessage message = originalEvent.getMessage();
		
		List<Map<String, String>> listA = getWorkerList(muleEventsWithoutException, 0);
		List<Map<String, String>> listB = getWorkerList(muleEventsWithoutException, 1);
		
		WorkersMerge workersMerge = new WorkersMerge();
		List<Map<String, String>> mergedWorkerList = workersMerge.mergeList(listA, listB);
		
		message.setPayload(mergedWorkerList);
		
		return new DefaultMuleEvent(message, originalEvent);
	}

	private List<Map<String, String>> getWorkerList(List<MuleEvent> events, int index) {
		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		if(events.get(index).getMessage().getPayload() instanceof ConsumerIterator){
			ConsumerIterator it = (ConsumerIterator) events.get(index).getMessage().getPayload();
			list = Lists.newArrayList(it);
		} else {
			List<Map<String, String>> lst = (List<Map<String, String>>) events.get(index).getMessage().getPayload();
			list = lst;
		}
		return list;
	}
}
