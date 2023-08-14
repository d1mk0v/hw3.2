package ru.hogwaarts.school.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwaarts.school.models.Avatar;
import ru.hogwaarts.school.models.Student;
import ru.hogwaarts.school.repositories.AvatarRepository;
import ru.hogwaarts.school.repositories.StudentRepository;
import ru.hogwaarts.school.services.api.AvatarService;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.LongStream;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
@Transactional
public class AvatarServiceImpl implements AvatarService {
    private final AvatarRepository avatarRepository;

    private final StudentRepository studentRepository;

    Logger logger = LoggerFactory.getLogger(AvatarServiceImpl.class);

    @Value("${path.to.avatars.folder}")
    private String avatarsDir;

    public AvatarServiceImpl(AvatarRepository avatarRepository, StudentRepository studentRepository) {
        this.avatarRepository = avatarRepository;
        this.studentRepository = studentRepository;
    }

    public void setAvatarsDir(String avatarsDir) {
        this.avatarsDir = avatarsDir;
    }

    public AvatarRepository getAvatarRepository() {
        return avatarRepository;
    }

    public StudentRepository getStudentRepository() {
        return studentRepository;
    }

    @Override
    public void uploadAvatar(Long id, MultipartFile avatarFile) throws IOException {

        logger.info("Was invoked method for upload avatar");

        Student student = studentRepository.getReferenceById(id);

        Path filePath = Path.of(avatarsDir, student + "." + getExtensions(avatarFile.getOriginalFilename()));
        Files.createDirectories(filePath.getParent());
        Files.deleteIfExists(filePath);

        try (
                InputStream is = avatarFile.getInputStream();
                OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
                BufferedInputStream bis = new BufferedInputStream(is, 1024);
                BufferedOutputStream bos = new BufferedOutputStream(os, 1024)
        ) {
            bis.transferTo(bos);
        }
        Avatar avatar = findAvatar(id);
        avatar.setStudent(student);
        avatar.setFilePath(filePath.toString());
        avatar.setFileSize(avatarFile.getSize());
        avatar.setMediaType(avatarFile.getContentType());
        avatar.setData(generateDataForDB(filePath));
        avatarRepository.save(avatar);
    }

    private byte[] generateDataForDB(Path filePath) throws IOException {

        logger.info("Was invoked method for generate data for DB");

        try (
                InputStream is = Files.newInputStream(filePath);
                BufferedInputStream bis = new BufferedInputStream(is, 1024);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BufferedImage image = ImageIO.read(bis);

            int height = image.getHeight() / (image.getWidth() / 100);

            BufferedImage preview = new BufferedImage(100, height, image.getType());
            Graphics2D graphics2D = preview.createGraphics();
            graphics2D.drawImage(image, 0, 0, 100, height, null);
            graphics2D.dispose();

            ImageIO.write(preview, getExtensions(filePath.getFileName().toString()), baos);
            return baos.toByteArray();
        }
    }

    private String getExtensions(String filename) {
        logger.info("Was invoked method for get extensions");
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    @Override
    public Avatar findAvatar(Long id) {
        logger.info("Was invoked method for find avatar");
        return avatarRepository.findByStudentId(id).orElse(new Avatar());
    }

    @Override
    public List<Avatar> getAllAvatars(Integer pageNumber, Integer pageSize) {
        logger.info("Was invoked method for get all avatars");
        PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize);
        return avatarRepository.findAll(pageRequest).getContent();
    }

    public String getAvatarsDir() {
        logger.info("Was invoked method for get avatars directory");
        return avatarsDir;
    }

    @Override
    public Long calculation() {
        return LongStream.rangeClosed(1, 1_000_000)
                .parallel()
                .sum();
    }
}
