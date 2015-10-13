package edu.harvard.hul.ois.fits

import java.io.File
import groovy.io.FileType

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.OptionGroup
import org.apache.commons.cli.Options
import org.apache.log4j.Logger
import org.apache.log4j.PropertyConfigurator

import org.custommonkey.xmlunit.*

class FitsCommandProcessor {
	
	public static final String[] skipList = [".DS_Store"]
	
	// Read Data from the config file
	// Get an instance of the configReaderSingleton class
	def configReader = new ConfigReader()
	def config = configReader.getConfig()
	
	private static boolean debugCmd;
	static Logger log = Logger.getLogger(FitsCommandProcessor.class.getName())

	static main(args) {
		
		Properties props = new Properties()
		props.load(new FileInputStream("log4j.properties"))
		PropertyConfigurator.configure(props)
		
		// DEBUG
		debugCmd=true
		
		Options options = new Options()
		options.addOption( "i", true, "input file or directory" )
		options.addOption( "o", true, "output directory" );
		options.addOption( "r", false, "recurse input directory (if it is a directory)" );
		options.addOption( "h", false, "print this message" )
		options.addOption( "d", false, "debug command line arguments and properties" )
		options.addOption( "c", false, "compare the actual output with expected output" )
		OptionGroup outputOptions = new OptionGroup()
		Option stdxml = new Option( "x", false, "convert FITS output to a standard metadata schema" )
		Option combinedStd = new Option( "xc", false, "output using a standard metadata schema and include FITS xml" )
		outputOptions.addOption( stdxml )
		outputOptions.addOption( combinedStd )
		options.addOptionGroup( outputOptions )

		CommandLineParser parser = new DefaultParser()
				
		CommandLine cmd = parser.parse( options, args )
	
		if (cmd.hasOption( "h" )) {
		  printHelp( options )
		  System.exit( 0 )
		}
		
		FitsCommandProcessor app = new FitsCommandProcessor()
		def outputTypeStr = app.getOutputFromCommand(cmd)
		
		// Must have -i
		if (!cmd.hasOption( "i" )) {
			println "Missing input files"
			printHelp( options )
			System.exit(-1)
		}
		File startFolderOrFile = new File(cmd.getOptionValue( "i" ))
		
		// -o must be a directory
		if (!cmd.hasOption( "o" )) {
			println "Missing output folder"
			printHelp( options )
			System.exit(-1)
		}
		String outputRoot = cmd.getOptionValue( "o" )
		if (outputRoot == null || !(new File( outputRoot ).isDirectory())) {
			println  "The output root location must be a directory"
			printHelp( options )
			System.exit(-1)
		}
		
		boolean doRecurse = false
		if(cmd.hasOption( "r" )) {
			doRecurse = true
		}

		boolean doOutputCompare = false
		if(cmd.hasOption( "c" )) {
			doOutputCompare = true
		}
		
		println "STARTING - the FITS Command Line Test Application"
		log.info ("STARTING - the FITS Command Line Test Application")
		log.info ("Using Groovy: ${GroovySystem.version}")
		println "Using Groovy: ${GroovySystem.version}"
		
		if (startFolderOrFile.directory) {
			String startFolderStr = startFolderOrFile.absolutePath
			
			if(doRecurse == true) {
				startFolderOrFile.eachFileRecurse {fileOrFolder ->
					app.processFileOrFolder(fileOrFolder, outputRoot, outputTypeStr, 
						startFolderStr, doOutputCompare)
				}
			}
			else {	// no recursion, only process files
				startFolderOrFile.eachFile(FileType.FILES){ fileToProcess ->
					app.processFileOrFolder(fileToProcess, outputRoot, outputTypeStr,
						startFolderStr, doOutputCompare)
				}
			}
		}	// isDirectory
		else {
			String startFolderString = startFolderOrFile.absolutePath
			app.processFileOrFolder(startFolderOrFile, outputRoot, outputTypeStr, startFolderString, doOutputCompare)
		}
		
		println "SHUTDOWN - the FITS Command Line Test Application"
		log.info ("SHUTDOWN - the FITS Command Line Test Application")
	
	}
	
	private processFileOrFolder(File fileOrFolder, String outputRoot, String outputTypeStr, String startFolderStr,
		boolean doOutputCompare) {
		
		//println fileOrFolder
		
		// Skip certain files, such as .DS_Store
		if(!FitsCommandProcessor.skipList.contains(fileOrFolder.name)) {

			// If the file is a folder, create the subfolders, to preserve them,
			// Otherwise, process the file in FITS
			if(fileOrFolder.isDirectory()) {
				def folderStr = fileOrFolder.absolutePath.minus(startFolderStr)
				println "FOLDER -> ${folderStr} goes under ${outputRoot}"
				
				// NOTE: should I use mkdirs() to make sure all directories are there?
				def newFolder = new File("${outputRoot}/${folderStr}").mkdir()
			}
			else {
				println fileOrFolder
				def fileStr = fileOrFolder.absolutePath.minus(fileOrFolder)
				println "\t\tOUTPUT_LOCATION is ${outputRoot}${fileStr}"
				def fileparts = fileOrFolder.name.split("\\.")
				if(fileparts.size() > 1) {
					def extension = fileparts[fileparts.size ()- 1]
					def folderStr = fileOrFolder.absolutePath.minus(startFolderStr).minus(fileOrFolder.name)
					def folderStr_def = "/"
					if(folderStr == null || folderStr.length()<1)
						folderStr = folderStr_def
					
					String outputFileName = "${outputRoot}${folderStr}${fileOrFolder.name}.xml"
					
					//println "extention is def names = (file.name.split("\\.")"
					println "\t\textension is: ${extension}"
					println "THE OUTPUT FILE is ${outputFileName}"
					
					//println "Calling FITS to create ${outputFileName}"
					
					callFits(fileOrFolder, outputFileName, outputTypeStr)
					
					// Compare results if necessary
					if (doOutputCompare) {
						
						// TODO: Initialize these only once
						def standardFolder = config.test.fits.expected.Standard.folder
						def fitsFolder = config.test.fits.expected.Fits.folder
						def comboFolder = config.test.fits.expected.Combo.folder
						def expectedRoot = config.test.fits.expected.root.dir
						
						// Select the output subfolder based on the output type
						// TODO: verify the below config settings BEFORE using them
						//def standardFolder = config.test.fits.expected.Standard.folder
						//def fitsFolder = config.test.fits.expected.Fits.folder
						//def comboFolder = config.test.fits.expected.Combo.folder
						String fileTypePrefix = standardFolder
						if (outputTypeStr.equalsIgnoreCase("Fits")) {
							fileTypePrefix = fitsFolder
						}
						else if (outputTypeStr.equalsIgnoreCase("Combined")) {
							fileTypePrefix = comboFolder
						}
						
						//compareResults(File file, String fileTypePrefix, String testOutputDirField)
						File outputFile = new File(outputFileName)
						//// TODO: outputRoot might not be where the expected files are
						//// Create a configurable var for the expected output root
						////String expectedRoot = "/Users/dab980/documents/FITS_TEST/FITS_Test_Expected"
						String expectedFileName = "${expectedRoot}/${fileTypePrefix}${folderStr}${fileOrFolder.name}.xml"
						
						// Complete the below to point to the correct file
						compareResults("${outputFileName}", "${expectedFileName}")
					}
					
				}

			}
			
		}
		
	}
	
	private static void printHelp( Options opts ) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "fits", opts );
	}
	
	private static String getOutputFromCommand(CommandLine cmd) {
		
		if (cmd.hasOption("-xc"))
			return outputTypeLocal.COMBINED
		else if (cmd.hasOption( "x" ))
			return outputTypeLocal.STANDARD
		else
			return  outputTypeLocal.FITS
	}
	
	public enum outputTypeLocal {
		COMBINED, STANDARD, FITS
	}
	
	public void callFits(File inputFile, String outputFileName, String outputTypeStr) {
		// Path to FITS script or batch file
		def fitsInstallDir = config.test.fits.install.dir
		def fitsStartScript = config.test.fits.runner
		def fitsScriptFile = new File("${fitsInstallDir}/${fitsStartScript}")
		
		// Verify the FITS directory exists before going any further
		File runDir = new File(fitsInstallDir)
		if( !runDir.exists() ) {
			println "FITS directory ${runDir.absolutePath} does not exist. "
			System(-1)

		}

		// println "Calling FITS to create ${outputFileName}"		
		callFits(fitsScriptFile, inputFile, runDir, outputTypeStr, outputFileName)
	}
	
	void callFits(File fitsFile, File fileToProcess, File runDir, String outputTypeStr,
		String outputFileName) {
		
		// --------------------------------------------------------------------
		// Create a Process Builder object
		// --------------------------------------------------------------------
		// DEBUG
		// println "Output Type " + outputType
		
		def outputToFileSwitch = "-o"

		// DEBUG
		// println "Arg for output file: ${outputToFileSwitch} ${outputFileName}"
		
		println "${FitsTester_MainGui.newline}File to process is: ${fileToProcess}"
		//textArea.append("${FitsTester_MainGui.newline}${fileOrDirMsg} to process by FITS is: ${fileToProcess}${FitsTester_MainGui.newline}")
		log.info ("File to process by FITS is: ${fileToProcess}")
		
		println "Processing ... Please wait ..."
		
		ProcessBuilder pb = new ProcessBuilder(fitsFile.getAbsolutePath(),
			"-i", fileToProcess.getAbsolutePath(), outputTypeStr,
			outputToFileSwitch, outputFileName)

		// Set the working directory. The program will run as if you are in this
		// directory.
		pb.directory(runDir)

		// Redirect the error stream (merging both both std out and error stream)
		pb.redirectErrorStream(true)

		// Start the process and wait for it to finish.
		final Process process = pb.start()

//		// --------------------------------------------------------------------
//		// Handle the output stream (both std out and error stream are merged)
//		// ONLY If we are not outputting to a file
//		// --------------------------------------------------------------------
//		if(!outputToFile) {
//			InputStreamReader isr = new  InputStreamReader(process.getInputStream())
//			BufferedReader br = new BufferedReader(isr)
//			String lineRead
//	
//			while ((lineRead = br.readLine()) != null) {
//				//log.info "FITS_OUT_STREAM >>> " + lineRead
//				println "FITS_OUT_STREAM >>> " + lineRead
//				//textArea.append("FITS_OUT_STREAM >>> ${lineRead}${newline}")
//			}
//		}

		// --------------------------------------------------------------------

		// 0 indicates normal termination
		boolean processSuccess = (process.waitFor() == 0)

		println "Processed file ${fileToProcess} finished with status: " +
			"${processSuccess ? "SUCCESS" :  "FAILURE"}"

		//textArea.append("Processed ${fileOrDirMsg} ${fileToProcess} finished with status: " +
		//	"${processSuccess ? "SUCCESS" :  "FAILURE"}" +
		//	"${newline}${newline}")
		
		log.info ("Processed file ${fileToProcess} finished with status: " +
			"${processSuccess ? "SUCCESS" :  "FAILURE"}")

		
	} // callFits()
		

	void compareResults(String inputFileName, String expectedFileName) {
		
		println "Now comparing ${inputFileName} to ${expectedFileName}"
		log.info ("Now comparing ${inputFileName} to ${expectedFileName}")

		TestUtil app = new TestUtil()
		
		Diff diff = app.compareXmlFiles(new File(expectedFileName), new File(inputFileName))

		//def expectedDirPath = config.test.fits.expected.root.dir
		println "Is Identical: " + diff.identical()
		log.info("Is Identical: " + diff.identical())

		if (!diff.identical()) {	// add to diffList

			DetailedDiff detailedDiff = new DetailedDiff(diff);
			// Display any Differences
			List<Difference> diffs = detailedDiff.getAllDifferences()
			StringBuffer differenceDescription = new StringBuffer()
			differenceDescription.append(diffs.size()).append(" differences")

			System.out.println(differenceDescription.toString())
			for(Difference difference : diffs) {
				System.out.println("${inputFileName}: " + difference.toString())
				log.error ("${inputFileName}: " + difference.toString())
			}

		}

	} // compareResults


}
