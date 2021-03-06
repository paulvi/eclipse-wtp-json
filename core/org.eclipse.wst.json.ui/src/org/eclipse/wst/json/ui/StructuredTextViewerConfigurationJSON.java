package org.eclipse.wst.json.ui;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.wst.json.core.text.IJSONPartitions;
import org.eclipse.wst.json.ui.internal.style.LineStyleProviderForJSON;
import org.eclipse.wst.sse.core.text.IStructuredPartitions;
import org.eclipse.wst.sse.ui.StructuredTextViewerConfiguration;
import org.eclipse.wst.sse.ui.internal.provisional.style.LineStyleProvider;

/**
 * Configuration for a source viewer which shows JSON.
 * <p>
 * Clients can subclass and override just those methods which must be specific
 * to their needs.
 * </p>
 * 
 * @see org.eclipse.wst.sse.ui.StructuredTextViewerConfiguration
 */
public class StructuredTextViewerConfigurationJSON extends
		StructuredTextViewerConfiguration {

	/*
	 * One instance per configuration because not sourceviewer-specific and it's
	 * a String array
	 */
	private String[] fConfiguredContentTypes;

	/*
	 * One instance per configuration
	 */
	private LineStyleProvider fLineStyleProviderForJSON;

	public LineStyleProvider[] getLineStyleProviders(
			ISourceViewer sourceViewer, String partitionType) {
		LineStyleProvider[] providers = null;

		if ((partitionType == IJSONPartitions.JSON)) {
			providers = new LineStyleProvider[] { getLineStyleProviderForJSON() };
		}

		return providers;
	}

	private LineStyleProvider getLineStyleProviderForJSON() {
		if (fLineStyleProviderForJSON == null) {
			fLineStyleProviderForJSON = new LineStyleProviderForJSON();
		}
		return fLineStyleProviderForJSON;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		if (fConfiguredContentTypes == null) {
			fConfiguredContentTypes = new String[] { IJSONPartitions.JSON,
					IStructuredPartitions.DEFAULT_PARTITION,
					IStructuredPartitions.UNKNOWN_PARTITION };
		}
		return fConfiguredContentTypes;
	}
}
