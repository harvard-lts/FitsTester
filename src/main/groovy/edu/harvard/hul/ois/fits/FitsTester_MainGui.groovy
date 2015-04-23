package edu.harvard.hul.ois.fits

import groovy.swing.SwingBuilder

import java.awt.BorderLayout
import java.io.File
import javax.swing.filechooser.FileFilter
import javax.swing.JFrame
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

class FitsTester_MainGui {

	private SwingBuilder swing
	
	String testOutputDir
	def filesToTest = new ArrayList<File>()
	def textArea
	def newline = "\n"
	
	def STANDARD_ARG = "-x"
	def COMBO_ARG = "-xc"
	
	// Read Data from the config file
	// Get an instance of the configReaderSingleton class
	def configReader = new ConfigReader()
	def config = configReader.getConfig()

	public FitsTester_MainGui() {

		def BL = new BorderLayout()
		//def log = ""

		def initialPath = System.getProperty("user.dir")

		swing = new SwingBuilder()
		
		// edt method makes sure UI is build on Event Dispatch Thread.
		swing.edt {
			//dirChooser = fileChooser()
			//dirChooser = fileChooser(
			dirChooser = new JFileChooser(
				dialogTitle: "Choose a Directory for Test Output",
				fileSelectionMode: JFileChooser.DIRECTORIES_ONLY,
				//the file filter must show also directories, in order to be able to look into them
				//fileFilter: [getDescription: {-> "Directories Only"}, accept:{file.isDirectory()}] as FileFilter)
				fileFilter: [getDescription: {-> "Directories Only"}, accept:{file.isDirectory()}] as FileFilter)

			lookAndFeel 'nimbus'  // Simple change in look and feel.
			frame = swing.frame(title:'FITS Tester', size:[800, 400],
			defaultCloseOperation: JFrame.DISPOSE_ON_CLOSE,			
			show:true, layout:new

			BorderLayout()) {
			
			
				menuBar {
					menu(text:'Tools') {
						menuItem() {
							//action(name:'Create Expected Results',this.&showCreateTestsDialog)
						}
					}
					menu(text:'Help') {
						menuItem() {
							action(name:'About', closure:{ showAbout() })
						}
						separator()
						menuItem() {
							action( name:'Exit', mnemonic:'E', closure:{ System.exit(0) } )
						}
					}
				}	// menuBar
							
				panel(constraints:BL.NORTH) {
					
					tableLayout {
						tr {
							td { // text property is default, so it is implicit.
								label 'Select Files to Test:'
							}
							td{
								button("...", actionPerformed: this.&selectFilesForTest)
							}
						}
						
						tr {
							td {
								label 'Test Output Directory:'
							}
							td {
								textField testOutputDir, id: 'testOutputDirField', columns: 50
							}
							td{
								button("...", actionPerformed: this.&selectOutputDir)
							}
						}
						
						tr {
							td {
								checkBox(id: 'fileOutputOn', text: 'Enable output to file')
							}
						}
						
						tr {
							td {
								label 'Output Type:'
							}
							td {
								comboBox(id: 'outputType', items:["FITS", "Standard", "Combo"], 
									selectedIndex:1);
							}
						}
						
						
					} // tablelayout				
					
				} // panel
				
				scrollPane(constraints:BL.CENTER) {
					textArea = swing.textArea()
				}
				panel(constraints:BL.SOUTH){
					button("Run", actionPerformed: this.&runTest)
					button(text:"Exit", actionPerformed:{ frame.dispose() })
				}

			} // BorderLayout

		}  // swing.edt

	}
	
//	private void selectOutputDir_NEW( event = null ) {
//		swing.with {
//			
//			
//			def fileChooser = swing.fileChooser(fileFilter: new JarFileChooserFilter())
//			if (fileChooser.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION) {
//				//File f = fileChooser.getSelectedFile()
//				//openFileInTab f
//			}
//			
////			JFileChooser chooseDir = new JFileChooser(
////				dialogTitle: "Choose a Directory for Test Output",
////				fileSelectionMode: JFileChooser.DIRECTORIES_ONLY,
////				fileFilter: new JarFileChooserFilter()
////				//,
////				//the file filter must show also directories, in order to be able to look into them
////				//fileFilter: [getDescription: {-> "Directories Only"}, accept:{file.isDirectory() }] as FileFilter
////				)
////			chooseDir.showOpenDialog(frame)
////			
////			doOutside {
////				doLater {
////					println chooseDir.getSelectedFile()
////					String selection = chooseDir.getSelectedFile()
////					if (selection != null && selection.length() > 0)
////						testOutputDirField.setText(selection)
////				}
////			}
//
//		} // swing.with
//	}
//	
//	class JarFileChooserFilter extends FileFilter {
//		
//			public boolean accept(File file) {
//				if (file.isDirectory()) {
//					return true
//				//} else {
//				//	return file.name =~ /.*\.[jwear]ar/
//				}
//			}
//		
//			public String getDescription() {
//				return 'Directories Only'
//			}
//		
//	//	}
//	}
	
	private void selectOutputDir( event = null ) {
		swing.with {
			//dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
			//
			//dirChooser.setAcceptAllFileFilterUsed(false)
			////dirChooser.setDescription("Directories Only")
			
			int answer = dirChooser.showOpenDialog(frame)
			if( answer == JFileChooser.APPROVE_OPTION ) {
				doOutside {
					doLater {
						testOutputDirField.setText(dirChooser.getSelectedFile().getPath())
					}
				}
			}
		} // swing.with
	}
	
	private void selectFilesForTest( event = null) {
		filesToTest.clear()
		File[] files = null
		
		swing.with {
			JFileChooser chooser = new JFileChooser(
				dialogTitle: "Choose files or folder to pass to FITS",
				fileSelectionMode: JFileChooser.FILES_AND_DIRECTORIES)
			chooser.setMultiSelectionEnabled(true)
			chooser.showOpenDialog(frame)

			chooser.getSelectedFiles().each {
				filesToTest.add(it)
				// DEBUG
				println file:it.getName()
			}
		} // swing.with
		
	}
	
	private void runTest( event = null ) {
		swing.with {
			
			def fileTypeArg = ""
			String selected = outputType.selectedItem
			if (selected.equals("Standard")) {
				fileTypeArg = STANDARD_ARG
		    }
			else if (selected.equals("Combo")) {
				fileTypeArg = COMBO_ARG
			}
			
			//println filesToTest.size
			if (filesToTest.size < 1) {
				JOptionPane.showMessageDialog(null,
					"Please select some files to test.",
					"Test Files Specified",
					JOptionPane.WARNING_MESSAGE);
				return
			}
			
			if (testOutputDirField.text.length() == 0 ) {
				JOptionPane.showMessageDialog(null,
					"Please specify the Test Output Directory.",
					"Directory Not Specified",
					JOptionPane.WARNING_MESSAGE);
				return
			}

			File f1 = new File(testOutputDirField.text);
			if(!f1.exists()) {
				JOptionPane.showMessageDialog(null,
						"The Test Output Directory does not exist.",
						"Directory Not Found",
						JOptionPane.WARNING_MESSAGE);
				return
			}

			// Path to FITS script or batch file
			def FITS_DIR = config.test.fits.install.dir
			def FITS_PROG = config.test.fits.runner
			def fitsScriptFile = new File("${FITS_DIR}/${FITS_PROG}");
			
			// TODO:
			// This doesn't appear in realtime. It should
			//textArea.append( "Processing ..." + newline)
			textArea.append( "Processing ... Please wait ...${newline}${newline}")
			
			//try{
				//def String[] args = [testOutputDirField.text]
				filesToTest.each { file ->
					
					// textArea.append( "\t" + file.toString() + newline)
					
					// DEBUG
					// println "Is Output to file: " + fileOutputOn.selected
					
					callFits(fitsScriptFile, file, FITS_DIR, fileTypeArg, 
						fileOutputOn.selected, testOutputDirField.text)		
					

				} // filesToTest.each
			
				// TODO: Why won't this show up?
				textArea.append( " done" + newline)
				
			//}catch(Exception e){
				//Font font = new Font("Serif", Font.BOLD, 20)
				//textArea.setFont(font)
				//textArea.setForeground(Color.red)
				//textArea.setText(e.toString())
			//}
						
		}
	}
	
	void callFits(File fitsFile, File fileToProcess, String fitsDir, String outputType, 
		boolean outputToFile, String outputDirPath) {
		
		// --------------------------------------------------------------------
		// Create a Process Builder object
		// --------------------------------------------------------------------
		// DEBUG
		// println "Output Type " + outputType
		
		// Default output file params to empty strings
		def outputFileName = ""
		def outputToFileSwitch = ""
		if(outputToFile) {
			outputToFileSwitch = "-o"
			
			// If the file to process is a directory, the output must be 
			// the output directory, not a file
			//
			// Ex call:
			// ./fits.sh -i /Users/dab980/downloads/remade-video-files
			// -o /Users/dab980/documents/FITS_TEST/FITS_Test_Output
			//
			// TODO: Add validation for this
			println "File or folder to process is: ${fileToProcess}"
			textArea.append("File or folder to process is: ${fileToProcess}${newline}")
			if (fileToProcess.isDirectory()) {
				outputFileName =  outputDirPath
			}
			else {
				outputFileName =  outputDirPath + "/" +
					fileToProcess.name + ".xml"
			}
		}
		// DEBUG
		// println "Arg for output file: ${outputToFileSwitch} ${outputFileName}"
		
		ProcessBuilder pb = new ProcessBuilder(fitsFile.getAbsolutePath(),
			"-i", fileToProcess.getAbsolutePath(), outputType, 
			outputToFileSwitch, outputFileName);

		// Set the working directory. The program will run as if you are in this
		// directory.
		def runDir = new File(fitsDir)
		pb.directory(runDir);

		// Redirect the error stream (merging both both std out and error stream)
//		pb.redirectErrorStream(true)
		
		//
		// TODO: Redirect the output to the GUI's TextArea
		//

		// Add an environment variable to the process
		//pb.environment().put("-i", fileToProcess.getAbsolutePath())
		//pb.environment().put("isProduction", IS_PRODUCTION)
		//pb.environment().put("isTestData", IS_TEST)

		// Start the process and wait for it to finish.
		final Process process = pb.start();

		// --------------------------------------------------------------------
		// Handle the output stream (both std out and error stream are merged)
		// --------------------------------------------------------------------
		InputStreamReader isr = new  InputStreamReader(process.getInputStream());
		BufferedReader br = new BufferedReader(isr);
		String lineRead;

		while ((lineRead = br.readLine()) != null) {
			// swallow the line, or print it out
			//if (LOG_PROCESSOR.toLowerCase().equals("true")) {
			//	log.info lineRead
			//}
			println lineRead
			textArea.append(lineRead)
		}
		// --------------------------------------------------------------------

		// 0 indicates normal termination
		boolean processSuccess = (process.waitFor() == 0)

		println "Processed directory finished with status: " +
				"${processSuccess ? "SUCCESS" :  "FAILURE"}${newline}"
		// TODO: Why won't this show up in real time?
		textArea.append("Processed directory finished with status: " +
				"${processSuccess ? "SUCCESS" :  "FAILURE"}" +
				"${newline}${newline}")
	}
	
	void showAbout() {
		JOptionPane.showMessageDialog(null,
			'This is the FITS Testing Application',
			'About',
			JOptionPane.INFORMATION_MESSAGE)
	}

	static main(args) {
		println "Using Groovy: ${GroovySystem.version}"
		FitsTester_MainGui viewer = new FitsTester_MainGui()
	}
}
