package org.eclipse.wst.json.core.internal.parser;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.wst.json.core.internal.Logger;
import org.eclipse.wst.json.core.regions.JSONRegionContexts;
import org.eclipse.wst.sse.core.internal.ltk.parser.RegionParser;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.sse.core.internal.util.Debug;

public class JSONSourceParser implements RegionParser {

	private long fStartTime;
	private long fStopTime;
	private JSONTokenizer fTokenizer;

	@Override
	public IStructuredDocumentRegion getDocumentRegions() {
		IStructuredDocumentRegion headnode = null;
		if (headnode == null) {
			if (Debug.perfTest) {
				fStartTime = System.currentTimeMillis();
			}
			headnode = parseNodes();
			if (Debug.perfTest) {
				fStopTime = System.currentTimeMillis();
				System.out
						.println(" -- creating nodes of IStructuredDocument -- "); //$NON-NLS-1$
				System.out
						.println(" Time parse and init all regions: " + (fStopTime - fStartTime) + " (msecs)"); //$NON-NLS-2$//$NON-NLS-1$
				// System.out.println(" for " + fRegions.size() + "
				// Regions");//$NON-NLS-2$//$NON-NLS-1$
				System.out
						.println("      and " + _countNodes(headnode) + " Nodes"); //$NON-NLS-2$//$NON-NLS-1$
			}
		}
		return headnode;
	}

	private IStructuredDocumentRegion parseNodes() {
		// regions are initially reported as complete offsets within the
		// scanned input
		// they are adjusted here to be indexes from the currentNode's start
		// offset
		IStructuredDocumentRegion headNode = null;
		IStructuredDocumentRegion lastNode = null;
		ITextRegion region = null;
		IStructuredDocumentRegion currentNode = null;
		String type = null;
		String currentRegionType = null;

		try {
		while ((region = getNextRegion()) != null) {
			type = region.getType();
			if (mustBeStart(type, currentRegionType) && currentNode != null) {
				currentNode.setEnded(true);
			}

			if ((currentNode != null && currentNode.isEnded())
					|| currentNode == null) {
				if (currentNode != null && !currentNode.isEnded()) {
					currentNode.setEnded(true);
				}
				lastNode = currentNode;
				currentNode = createStructuredDocumentRegion(type);
				currentRegionType = type;
				if (lastNode != null) {
					lastNode.setNext(currentNode);
				}
				currentNode.setPrevious(lastNode);
				currentNode.setStart(region.getStart());
			}

			currentNode.addRegion(region);
			currentNode.setLength(region.getStart() + region.getLength()
					- currentNode.getStart());
			region.adjustStart(-currentNode.getStart());

			if (mustBeEnd(type)) {
				currentNode.setEnded(true);
			}

			if (headNode == null && currentNode != null) {
				headNode = currentNode;
			}
		}

		}
		catch(Throwable e) {
			e.printStackTrace();
		}
		if (currentNode != null && !currentNode.isEnded()) {
			currentNode.setEnded(true);
		}

		primReset();
		return headNode;
	}

	/**
	 * Return the full list of known regions. Typically getNodes should be used
	 * instead of this method.
	 */
	@Override
	public List getRegions() {
		IStructuredDocumentRegion headNode = null;
		if (!getTokenizer().isEOF()) {
			headNode = getDocumentRegions();
			// throw new IllegalStateException("parsing has not finished");
		}
		// for memory recovery, we assume if someone
		// requests all regions, we can reset our big
		// memory consuming objects
		// but the new "getRegions" method is then more expensive.
		// I don't think its used much, though.
		List localRegionsList = getRegions(headNode);
		primReset();
		return localRegionsList;
	}

	/**
	 * Method getRegions.
	 * 
	 * @param headNode
	 * @return List
	 */
	protected List getRegions(IStructuredDocumentRegion headNode) {
		List allRegions = new ArrayList();
		IStructuredDocumentRegion currentNode = headNode;
		while (currentNode != null) {
			ITextRegionList nodeRegions = currentNode.getRegions();
			for (int i = 0; i < nodeRegions.size(); i++) {
				allRegions.add(nodeRegions.get(i));
			}
			currentNode = currentNode.getNext();
		}
		return allRegions;
	}

	@Override
	public RegionParser newInstance() {
		return new JSONSourceParser();
	}

	@Override
	public void reset(Reader reader) {
		primReset();
		getTokenizer().reset(reader, 0);
	}

	@Override
	public void reset(Reader reader, int offset) {
		reset(reader);
	}

	@Override
	public void reset(String input) {
		reset(new StringReader(input));
	}

	@Override
	public void reset(String input, int offset) {
		reset(input);
	}

	private IStructuredDocumentRegion createStructuredDocumentRegion(String type) {
		return JSONStructuredDocumentRegionFactory.createRegion(type);
	}

	/**
	 * currently public but may be made default access protected in future.
	 */
	protected boolean mustBeStart(String type, String docRegionType) {
		return type == JSONRegionContexts.JSON_OBJECT_OPEN
				|| type == JSONRegionContexts.JSON_ARRAY_OPEN;
		/*
		 * ((type == JSONRegionContexts.JSON_DELIMITER || type ==
		 * JSONRegionContexts.JSON_LBRACE || type ==
		 * JSONRegionContexts.JSON_RBRACE || type ==
		 * JSONRegionContexts.JSON_IMPORT || type ==
		 * JSONRegionContexts.JSON_PAGE || type == JSONRegionContexts.JSON_MEDIA
		 * || type == JSONRegionContexts.JSON_FONT_FACE || type ==
		 * JSONRegionContexts.JSON_CHARSET || type ==
		 * JSONRegionContexts.JSON_ATKEYWORD || type ==
		 * JSONRegionContexts.JSON_DECLARATION_PROPERTY || type ==
		 * JSONRegionContexts.JSON_DECLARATION_DELIMITER) || (docRegionType ==
		 * JSONRegionContexts.JSON_DECLARATION_PROPERTY && type ==
		 * JSONRegionContexts.JSON_S) || (!JSONRegionUtil
		 * .isSelectorBegginingType(docRegionType) && (type ==
		 * JSONRegionContexts.JSON_SELECTOR_ELEMENT_NAME || type ==
		 * JSONRegionContexts.JSON_SELECTOR_UNIVERSAL || type ==
		 * JSONRegionContexts.JSON_SELECTOR_PSEUDO || type ==
		 * JSONRegionContexts.JSON_SELECTOR_CLASS || type ==
		 * JSONRegionContexts.JSON_SELECTOR_ID || type ==
		 * JSONRegionContexts.JSON_SELECTOR_ATTRIBUTE_START)));
		 */
	}

	/**
	 * currently public but may be made default access protected in future.
	 */
	protected boolean mustBeEnd(String type) {
		return (type == JSONRegionContexts.JSON_ARRAY_CLOSE || type == JSONRegionContexts.JSON_OBJECT_CLOSE);
	}

	private ITextRegion getNextRegion() {
		ITextRegion region = null;
		try {
			region = getTokenizer().getNextToken();
			// DMW: 2/12/03 Removed state
			// if (region != null) {
			// fRegions.add(region);
			// }
			return region;
		} catch (StackOverflowError e) {
			Logger.logException(
					getClass().getName()
							+ ": input could not be parsed correctly at position " + getTokenizer().getOffset(), e); //$NON-NLS-1$
			throw e;
		} catch (Exception e) {
			Logger.logException(
					getClass().getName()
							+ ": input could not be parsed correctly at position " + getTokenizer().getOffset() + " (" + e.getLocalizedMessage() + ")", e); //$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
		}
		return null;
	}

	private void primReset() {
		getTokenizer().reset(new char[0]);
	}

	/**
	 * currently public but may be made default access protected in future.
	 */
	private JSONTokenizer getTokenizer() {
		if (fTokenizer == null) {
			fTokenizer = new JSONTokenizer();
		}
		return fTokenizer;
	}

	/**
	 * This is a simple utility to count nodes. Used only for debug statements.
	 */
	private int _countNodes(IStructuredDocumentRegion nodes) {
		int result = 0;
		IStructuredDocumentRegion countNode = nodes;
		while (countNode != null) {
			result++;
			countNode = countNode.getNext();
		}
		return result;
	}
}
