/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://dspace.org/license/
 */

package org.dspace.ctask.replicate;

import java.io.IOException;

import org.dspace.content.DSpaceObject;
import org.dspace.core.ConfigurationManager;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;

/**
 * VerifyAIP task will simply test for the presence of a replica representation
 * of the object in the remote store. It succeeds if found, otherwise fails.
 * 
 * @author richardrodgers
 * @see TransmitAIP
 */

public class VerifyAIP extends AbstractCurationTask
{
    private String archFmt = ConfigurationManager.getProperty("replicate", "packer.archfmt");
    
    // Group where all AIPs are stored
    private final String storeGroupName = ConfigurationManager.getProperty("replicate", "group.aip.name");

    /**
     * Performs the "Verify AIP" task.
     * <p>
     * Simply tests for presence of AIP in replica ObjectStore.
     * @param dso the DSpace Object to verify
     * @return integer which represents Curator return status
     * @throws IOException 
     */
    @Override
    public int perform(DSpaceObject dso) throws IOException
    {
        ReplicaManager repMan = ReplicaManager.instance();
        
        String objId = ReplicaManager.safeId(dso.getHandle()) + "." + archFmt;
        boolean found = repMan.objectExists(storeGroupName, objId);
        String result = "AIP for object: " + dso.getHandle() + " found: " + found;
        report(result);
        setResult(result);
        return found ? Curator.CURATE_SUCCESS : Curator.CURATE_FAIL;
    }
}