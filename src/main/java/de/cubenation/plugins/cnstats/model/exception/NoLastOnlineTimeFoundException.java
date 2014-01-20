package de.cubenation.plugins.cnstats.model.exception;

import org.apache.commons.lang.Validate;

import de.cubenation.plugins.utils.chatapi.ResourceConverter;
import de.cubenation.plugins.utils.exceptionapi.PlayerException;

public class NoLastOnlineTimeFoundException extends PlayerException {
    private static final long serialVersionUID = -735618359861722362L;

    public NoLastOnlineTimeFoundException(String playerName) {
        super("no last online time found for player " + playerName, playerName);
    }

    @Override
    public String getLocaleMessage(ResourceConverter converter) {
        Validate.notNull(converter, "converter cannot be null");

        return converter.convert("player.lastOnlineTimeNotFound", getPlayerName());
    }
}
