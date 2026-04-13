package org.cyclopsgroup.jmxterm.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class XdgDirectoriesTest {

  @Test
  void stateHomeUsesEnvVarWhenSet() {
    var dirs = new XdgDirectories(_ -> "/custom/state", "/home/testuser");
    assertThat(dirs.getStateHome()).isEqualTo(Path.of("/custom/state"));
  }

  @Test
  void stateHomeFallsBackWhenEnvVarIsNull() {
    var dirs = new XdgDirectories(_ -> null, "/home/testuser");
    assertThat(dirs.getStateHome()).isEqualTo(Path.of("/home/testuser/.local/state"));
  }

  @Test
  void stateHomeFallsBackWhenEnvVarIsBlank() {
    var dirs = new XdgDirectories(_ -> "  ", "/home/testuser");
    assertThat(dirs.getStateHome()).isEqualTo(Path.of("/home/testuser/.local/state"));
  }

  @Test
  void historyFileResolvesUnderStateHome() {
    var dirs = new XdgDirectories(_ -> "/xdg/state", "/ignored");
    assertThat(dirs.getHistoryFile()).isEqualTo(Path.of("/xdg/state/jmxsh/history"));
  }

  @Test
  void historyFileUsesDefaultStateHome() {
    var dirs = new XdgDirectories(_ -> null, "/home/testuser");
    assertThat(dirs.getHistoryFile())
        .isEqualTo(Path.of("/home/testuser/.local/state/jmxsh/history"));
  }

  @Test
  void legacyHistoryFileIsInHome() {
    var dirs = new XdgDirectories(_ -> null, "/home/testuser");
    assertThat(dirs.getLegacyHistoryFile()).isEqualTo(Path.of("/home/testuser/.jmxterm_history"));
  }
}
