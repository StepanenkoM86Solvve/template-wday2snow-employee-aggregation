package org.mule.templates.transformers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import com.google.common.collect.Lists;

/**
 * This transformer will take to list as input and create a third one that will be the merge of the previous two. The identity of an element of the list is
 * defined by its email.
 * 
 * @author cesar.garcia
 */
public class MergeEmployeesTransformer extends AbstractMessageTransformer {

	private static final String QUERY_COMPANY_A = "employeesFromOrgA";
	private static final String QUERY_COMPANY_B = "employeesFromOrgB";

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		List<Map<String, String>> mergedContactList = mergeList(getContactsList(message, QUERY_COMPANY_A), getContactsList(message, QUERY_COMPANY_B));

		return mergedContactList;
	}

	private List<Map<String, String>> getContactsList(MuleMessage message, String propertyName) {
		return message.<List<Map<String, String>>>getInvocationProperty(propertyName);
	}

	/**
	 * The method will merge the accounts from the two lists creating a new one.
	 * 
	 * @param contactsFromOrgA
	 *            contacts from organization A
	 * @param contactsFromOrgB
	 *            contacts from organization B
	 * @return a list with the merged content of the to input lists
	 */
	private List<Map<String, String>> mergeList(List<Map<String, String>> contactsFromOrgA, List<Map<String, String>> contactsFromOrgB) {
		List<Map<String, String>> mergedContactsList = new ArrayList<Map<String, String>>();

		// Put all contacts from A in the merged mergedContactsList
		for (Map<String, String> contactFromA : contactsFromOrgA) {
			Map<String, String> mergedContact = createMergedContact(contactFromA);
			mergedContact.put("IDInA", contactFromA.get("Id"));
			mergedContactsList.add(mergedContact);
		}

		// Add the new contacts from B and update the exiting ones
		for (Map<String, String> contactsFromB : contactsFromOrgB) {
			Map<String, String> contactFromA = findContactInList(contactsFromB.get("Email"), mergedContactsList);
			if (contactFromA != null) {
				contactFromA.put("IDInB", contactsFromB.get("Id"));
			} else {
				Map<String, String> mergedAccount = createMergedContact(contactsFromB);
				mergedAccount.put("IDInB", contactsFromB.get("Id"));
				mergedContactsList.add(mergedAccount);
			}

		}
		return mergedContactsList;
	}

	private Map<String, String> createMergedContact(Map<String, String> contact) {
		Map<String, String> mergedContact = new HashMap<String, String>();
		mergedContact.put("Name", contact.get("Name"));
		mergedContact.put("Email", contact.get("Email"));
		mergedContact.put("IDInA", "");
		mergedContact.put("IDInB", "");
		return mergedContact;
	}

	private Map<String, String> findContactInList(String accountName, List<Map<String, String>> orgList) {
		for (Map<String, String> account : orgList) {
			if (account.get("Email")
						.equals(accountName)) {
				return account;
			}
		}
		return null;
	}
}

