/*
* Copyright 2015 Harvard University Library
*
* This file is part of FITS (File Information Tool Set).
*
* FITS is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* FITS is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with FITS.  If not, see <http://www.gnu.org/licenses/>.
*/
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
        this.config
    }

}
