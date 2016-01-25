LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := hciviewer

LOCAL_CFLAGS := -std=gnu++11
LOCAL_CPPFLAGS += -fexceptions

INCLUDE_SRC_DIR       := ../hcidecoder/src
INCLUDE_EXTERNAL_DIR  := ../btsnoop-decoder/snoop-stream-lib

LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(INCLUDE_SRC_DIR)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/${INCLUDE_EXTERNAL_DIR}

LOCAL_SRC_FILES  := btsnoopmonitor.cpp hci_wrapper.cpp

#hcidecoder

HCI_DECODER_PATH := ../hcidecoder/src

LOCAL_C_INCLUDES += $NDK/sources/cxx-stl/gnu-libstdc++/4.8/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(HCI_DECODER_PATH)

LOCAL_SRC_FILES += $(HCI_DECODER_PATH)/jsoncpp.cpp \
	$(HCI_DECODER_PATH)/hcidecoder.cpp \
	$(HCI_DECODER_PATH)/command_complete.cpp

#btsnoopdecoder

BTSNOOP_DECODER_PATH := ../btsnoop-decoder/snoop-stream-lib/btsnoop

LOCAL_C_INCLUDES += $NDK/sources/cxx-stl/gnu-libstdc++/4.8/include

LOCAL_SRC_FILES += $(BTSNOOP_DECODER_PATH)/btsnoopfileinfo.cpp \
	$(BTSNOOP_DECODER_PATH)/btsnooppacket.cpp \
	$(BTSNOOP_DECODER_PATH)/btsnoopparser.cpp \
	$(BTSNOOP_DECODER_PATH)/btsnooptask.cpp

LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)