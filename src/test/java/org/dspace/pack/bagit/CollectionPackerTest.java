/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.pack.bagit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.collect.ImmutableList;
import org.dspace.content.Collection;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.core.Context;
import org.junit.Test;

/**
 * Tests for {@link CollectionPacker}
 */
public class CollectionPackerTest extends BagItPackerTest {

    // same as from CollectionPacker -- might be easier to make that public rather than duplicate
    private final ImmutableList<String> fields = ImmutableList.of("name", "short_description", "introductory_text",
                                                                  "provenance_description", "license", "copyright_text",
                                                                  "side_bar_text");

    @Test
    public void testPack() throws Exception {
        // get the output location
        final URL resources = CollectionPackerTest.class.getClassLoader().getResource("");
        assertNotNull(resources);
        final Path output = Paths.get(resources.toURI().resolve("collection-packer-test"));

        // init test Collection
        final Collection collection = initDSO(Collection.class);
        assertNotNull(collection);
        assertNotNull(collection.getID());
        assertNotNull(collection.getCommunities());
        assertTrue(collection.getCommunities().isEmpty());

        // grab our mocks
        final CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
        for (String field : fields) {
            when(collectionService.getMetadata(eq(collection), eq(field))).thenReturn(field);
        }

        final CollectionPacker collectionPacker = new CollectionPacker(collection, archFmt);
        final File packedOutput = collectionPacker.pack(output.toFile());

        for (String field : fields) {
            verify(collectionService, times(1)).getMetadata(eq(collection), eq(field));
        }

        assertThat(packedOutput).exists();
        assertThat(packedOutput).isFile();

        packedOutput.delete();
    }

    @Test
    public void testUnpack() throws Exception {
        // push to setup
        final URL resources = CollectionPackerTest.class.getClassLoader().getResource("unpack");
        assertNotNull(resources);

        final Path archive = Paths.get(resources.toURI()).resolve("COLLECTION@123456789-2.zip");
        final Path openArchive = Paths.get(resources.toURI()).resolve("COLLECTION@123456789-2");

        final Collection collection = initDSO(Collection.class);
        assertNotNull(collection);

        final CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
        final CollectionPacker packer = new CollectionPacker(collection, archFmt);
        packer.unpack(archive.toFile());

        verify(collectionService, times(7)).setMetadata(any(Context.class), eq(collection), anyString(), anyString());
        verify(collectionService, times(1)).setLogo(any(Context.class), eq(collection), any(InputStream.class));
        verify(collectionService, times(1)).update(any(Context.class), eq(collection));

        assertThat(openArchive).doesNotExist();
    }

}