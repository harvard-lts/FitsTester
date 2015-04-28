package edu.harvard.hul.ois.fits

import org.custommonkey.xmlunit.DetailedDiff

class NonMatchingResult {

	def actualXmlFileName
	def expectedXmlFileName
	DetailedDiff detailedDiff
}
