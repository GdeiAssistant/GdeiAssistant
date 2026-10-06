package cn.gdeiassistant.core.secret.service;

import cn.gdeiassistant.common.tools.Utils.AnonymizeUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecretAnonymityLabelTest {

    @Test
    void treeholeLabelNeverLooksLikeCampusUsername() {
        String label = AnonymizeUtils.treeholeAnonymousLabel();
        assertEquals("匿名用户", label);
        assertFalse(label.matches("[a-zA-Z0-9_]{3,24}"));
    }
}
