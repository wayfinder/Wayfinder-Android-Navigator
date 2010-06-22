#-------------------------------------------------------------------------------
# Copyright (c) 1999-2010, Vodafone Group Services
# All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without 
# modification, are permitted provided that the following conditions 
# are met:
# 
#     * Redistributions of source code must retain the above copyright 
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above 
#       copyright notice, this list of conditions and the following 
#       disclaimer in the documentation and/or other materials provided 
#       with the distribution.
#     * Neither the name of Vodafone Group Services nor the names of its 
#       contributors may be used to endorse or promote products derived 
#       from this software without specific prior written permission.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
# IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
# OF SUCH DAMAGE.
#-------------------------------------------------------------------------------
To be able to run the emulator you need to create an AVD-file.
AVD stands for Android Virtual Device and tells the navigator what platform version the application wants to run, and what APIs are included.

To create the AVD do the following:
1) Open a command prompt

2) run: "android list targets"

3) The output will be something like this:
 	C:\dev>android list targets
	Available Android targets:
	id: 1
	     Name: Android 1.1
	     Type: Platform
	     API level: 2
	     Skins: HVGA (default), HVGA-L, HVGA-P, QVGA-L, QVGA-P
	id: 2
	     Name: Android 1.5
	     Type: Platform
	     API level: 3
	     Skins: HVGA (default), HVGA-L, HVGA-P, HVGA-P-HTCMagic, QVGA-L, QVGA-P
	id: 3
	     Name: Google APIs
	     Type: Add-On
	     Vendor: Google Inc.
	     Description: Android + Google APIs
	     Based on Android 1.5 (API level 3)
	     Libraries:
	      * com.google.android.maps (maps.jar)
	          API for Google Maps
	     Skins: HVGA-P-HTCMagic, QVGA-P, HVGA-L, HVGA (default), QVGA-L, HVGA-P
	     
4) Create a new AVD by: "android create avd --name <your_avd_name> --target <targetID>"
	Example: android create avd --name AndroidNavigator1.5 --target 2
	
5) You will be asked if you want to create a hardware profile. Choose no to use the default.

For more information regarding AVD, check this URL: http://developer.android.com/guide/developing/eclipse-adt.html

6) To run the emulator from eclipse you need to set up the project: 
	* Choose Run->Run configurations...
	* Right-click Androind Application, choose New
	* Give the new configuration a proper name: AndroindNavigator
	* Fill in what Project you want to run
	* Choose what Activity should be started
	* Select the Target-tab
	* Choose Deployment Target -> automatic
	* Choose the previous created AVD
	* Options: if you are running the emulator on a lowres screen, and the emulator is too big, you can shrink it by adding parameters to the emulator:
		# in "Additional Emulator Command Line Options" add "-scale 0.8". This will scale the emulator to 80% of the default size. Valid scales are from 0.1 to 3
	* click "apply" and then "run"
		 

