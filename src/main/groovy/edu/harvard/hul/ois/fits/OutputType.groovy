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
