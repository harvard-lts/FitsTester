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

enum OutputType {
	
	Fits ("Fits"),
	Standard ("Standard"),
	Combined ("Combined")
	
	private String name;
	
	OutputType(String name) {
		this.name = name;
	}
	
	public String getName () {
		return name;
	}
	
	static public OutputType lookup(String name) {
		OutputType retMethod = null;
		for(OutputType method : OutputType.values()) {
			if (method.getName().equals(name)) {
				retMethod = method;
				break;
			}
		}
		return retMethod;
	}

}
