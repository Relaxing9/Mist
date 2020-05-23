/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist;

/**
 * Main library class to handle utils and other stuff
 * Will contain a lot of static utils for our convenience
 */
public final class Mist {

    //  -------------------------------------------------------------------------
    //  Static final settings across the plugin
    //  Stored in internal classes relating to their category
    //  -------------------------------------------------------------------------

    /**
     * Core options relating to the plugin
     */
    public static final class Core {

        /**
         * Core revision version, meaning each major refactor or change,
         * we increment this number
         */
        public static final int CORE_VERSION = 1;

        /**
         * The actual version of this core. Each new release, set a new version
         */
        public static final String VERSION = "1.0a";

    }

    /**
     * Options relating to file parsing
     */
    public static final class File {

        /**
         * The name (with extension) for the main config file
         */
        public static final String SETTINGS_NAME = "config.yml";

    }

}
