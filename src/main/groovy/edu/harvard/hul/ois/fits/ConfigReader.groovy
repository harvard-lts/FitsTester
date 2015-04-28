package edu.harvard.hul.ois.fits

//
// Singleton Groovy Class to read the FITS Tester Properties file
//
//

@Singleton(lazy = true, strict = false)
class ConfigReader {
	private ConfigObject config = null
	private String PROPERTIES_FILE_NAME = "fits_tester.properties"

	private ConfigReader() {
	
		if (config == null) {
			def props = new Properties()
			new File(PROPERTIES_FILE_NAME).withInputStream { 
				stream -> props.load(stream) 
			}
			config = new ConfigSlurper().parse(props)
		}
	}
	
    public ConfigObject getConfig() { 
        return this.config;
    }

}
