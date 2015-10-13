package edu.harvard.hul.ois.fits

class FitsCommandProcessorTest {

	static main(args) {
		
		String[] argsArr = [
			"-d",
			"-c",
			
			// recursive
			"-r",

			"-i",
			
			//"/Users/dab980/Documents/video/FITS-VIDEO-SAMPLE-FILES/FITS-SAMPLE-26.mov",
			"/Users/dab980/Documents/video/FITS-VIDEO-SAMPLE-FILES_wip",
			// No command line arguments, FITS output ONLY
			//
			// -xc --> Outputs the FITS output plus the FITS output transformed into standard XML schemas
			"-xc",
			//
			// -x --> Transforms the FITS output into standard XML schemas (EBUCore)
			//"-x",
			//
			"-o",
			"/Users/dab980/documents/FITS_TEST/output_wip"
		]

		FitsCommandProcessor.main(argsArr);
	
	}

}
