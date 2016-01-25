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
	btsnoopmonitor.cpp

	Monitoring implementation of ibtsnooplistener

	@author Bertrand Martel
	@version 0.1
*/

#include "btsnoopmonitor.h"
#include "btsnoop/ibtsnooplistener.h"
#include "btsnoop/btsnoopfileinfo.h"
#include "hci_decoder/IHciFrame.h"
#include "btsnoop/btsnooptask.h"

#include "jni.h"
#include <android/log.h>

using namespace std;

BtSnoopMonitor::BtSnoopMonitor()
{
	count=0;
	count2=0;
}

BtSnoopMonitor::~BtSnoopMonitor(){
}

/**
 * @brief onSnoopPacketReceived
 *      called when a new packet record has been received
 * @param fileInfo
 *      file info object
 * @param packet
 *      snoop packet record object
 * @param jni_env
 *      JNI env object
 */
void BtSnoopMonitor::onSnoopPacketReceived(BtSnoopFileInfo fileInfo,BtSnoopPacket packet,JNIEnv * jni_env){

	/*
	count++;
	__android_log_print(ANDROID_LOG_INFO,"hci-debugger","new frame %d\n",count);
	
	//packet.printInfo();
	*/

	IHciFrame * frame = hci_decoder.decode(packet.getPacketData());

	if (frame!=0){
		/*
		if (frame->getPacketType() == HCI_TYPE_EVENT){

			IHciEventFrame* eventFrame = dynamic_cast<IHciEventFrame*>(frame);

			if (eventFrame->getEventCode() == HCI_EVENT_LE_META){

				count2++;
				__android_log_print(ANDROID_LOG_INFO,"hcidecoder","new frame %d : decoded %d\n",count,count2);
				
				std::ostringstream oss;
				uint64_t i = packet.getUnixTimestampMicroseconds();
				oss << i;
				std:string intAsString(oss.str());
				__android_log_print(ANDROID_LOG_VERBOSE,"hcidecoder","frame : %s %s\n",intAsString.data(),frame->toJson(false).data());
			}
		}
		*/

		if (jni_env!=0){
			if (BtSnoopTask::jobj!=0 && BtSnoopTask::mid!=0){

				jstring hci_frame = jni_env->NewStringUTF(frame->toJson(false).data());

				jstring snoop_frame = jni_env->NewStringUTF(packet.toJson(false).data());

				jni_env->CallVoidMethod(BtSnoopTask::jobj, BtSnoopTask::mid, snoop_frame,hci_frame);

				jni_env->DeleteLocalRef(hci_frame);
				jni_env->DeleteLocalRef(snoop_frame);

				if (jni_env->ExceptionCheck()) {
					jni_env->ExceptionDescribe();
				}
			}
			else{
				__android_log_print(ANDROID_LOG_ERROR,"hci-debugger","class or method if not defined\n");
			}
		}
		else{
			__android_log_print(ANDROID_LOG_ERROR,"hci-debugger","jni_env is not defined\n");
		}
	}else{
		__android_log_print(ANDROID_LOG_INFO,"hci-debugger","frame not treated");
	}
}
