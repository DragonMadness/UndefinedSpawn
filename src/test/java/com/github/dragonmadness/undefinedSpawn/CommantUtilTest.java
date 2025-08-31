package com.github.dragonmadness.undefinedSpawn;

import com.github.dragonmadness.undefinedSpawn.command.util.CommandUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommantUtilTest {

    @Test
    public void testColorCodeReplacement() {
        Assertions.assertEquals(
                "§7test§fstring§raaaa",
                CommandUtil.translateColorCodes("&7test&fstring&raaaa")
        );
    }

}
