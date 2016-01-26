/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Bertrand Martel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
/**
	hci_wrapper.cpp

	Android wrapper for btsnoop decoder and hci frame decoder

	@author Bertrand Martel
	@version 0.1
*/

#include "btsnoop/btsnoopparser.h"
#include "btsnoopmonitor.h"
#include "btsnoop/btsnooptask.h"
#include "android/log.h"
#include <jni.h>

using namespace std;

static BtSnoopParser *parser_ptr;

extern "C" {

JNIEXPORT void JNICALL Java_fr_bmartel_bluetooth_hcidebugger_activity_HciDebuggerActivity_startHciLogStream(JNIEnv* env, jobject obj,jstring filePath)
{
	BtSnoopTask::jobj = env->NewGlobalRef(obj);

	// save refs for callback
	jclass g_clazz = env->GetObjectClass(BtSnoopTask::jobj);
	if (g_clazz == NULL) {
		__android_log_print(ANDROID_LOG_ERROR,"startHciLogStream","Failed to find class\n");
	}

	BtSnoopTask::mid = env->GetMethodID(g_clazz, "onHciFrameReceived", "(Ljava/lang/String;Ljava/lang/String;)V");
	if (BtSnoopTask::mid == NULL) {
		__android_log_print(ANDROID_LOG_ERROR,"startHciLogStream","Unable to get method ref\n");
	}

	BtSnoopParser parser;
	parser_ptr=&parser;

	BtSnoopMonitor monitor;

	parser.addSnoopListener(&monitor);

	const char* filePathConvert = env->GetStringUTFChars(filePath, 0);

	string filePathStr = filePathConvert;
	
	__android_log_print(ANDROID_LOG_VERBOSE,"startHciLogStream","opening HCI log file in %s\n",filePathStr.data());

	bool success = parser.decode_streaming_file(filePathStr);
	//bool success = parser.decode_streaming_file("/sdcard/btsnoop_hci.log");

	if (success){
	    //success
	    __android_log_print(ANDROID_LOG_VERBOSE,"startHciLogStream","file opened successfully");
	}
	else{
	    //failure (bad reading / file not found)
	    __android_log_print(ANDROID_LOG_VERBOSE,"startHciLogStream","fail to open file");
	}

	env->ReleaseStringUTFChars(filePath, filePathConvert);

}
}

extern "C" {

JNIEXPORT void JNICALL Java_fr_bmartel_bluetooth_hcidebugger_activity_HciDebuggerActivity_stopHciLogStream(JNIEnv* env, jobject obj)
{
	__android_log_print(ANDROID_LOG_VERBOSE,"stopHciLogStream","stopping thread\n");
	
	if (parser_ptr!=0){
		parser_ptr->stop();
		parser_ptr->clearListeners();
	}
}
}

extern "C" {

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* aReserved)
{
	BtSnoopTask::jvm = vm;

	return JNI_VERSION_1_6;
}
}