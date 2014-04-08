package net.wasnot.music.simplemusicplayer.service;

import net.wasnot.music.simplemusicplayer.service.IPlayerServiceCallback;

interface IPlayerService {
	/**
	 * コールバック登録。
	 * @param callback 登録するコールバック。
	 */
	oneway void registerCallback(IPlayerServiceCallback callback);
	
	/**
	 * コールバック解除。
	 * @param callback 解除するコールバック。
	 */
	oneway void unregisterCallback(IPlayerServiceCallback callback);
	
	void request();
}
