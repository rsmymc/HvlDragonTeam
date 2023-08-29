package com.hvl.dragonteam.Interface;

import android.content.Intent;

public interface OnIntentReceived<T, R> {
	void onIntent(int requestCode, int resultCode, Intent data);
}
